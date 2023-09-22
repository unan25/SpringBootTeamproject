package com.springboot.shootformoney.member.controllers;

import com.springboot.shootformoney.bet.entity.Bet;
import com.springboot.shootformoney.bet.service.EuroService;
import com.springboot.shootformoney.member.dto.SearchInfo;
import com.springboot.shootformoney.member.dto.MemberInfo;
import com.springboot.shootformoney.member.dto.SignUpForm;
import com.springboot.shootformoney.member.entity.Euro;
import com.springboot.shootformoney.member.services.MemberDeleteService;
import com.springboot.shootformoney.member.services.MemberListService;
import com.springboot.shootformoney.member.services.MemberPwCheckService;
import com.springboot.shootformoney.member.services.MemberUpdateService;
import com.springboot.shootformoney.member.utils.MemberUtil;
import com.springboot.shootformoney.member.validators.UpdateValidator;
import com.springboot.shootformoney.post.Post;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/member/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberUtil memberUtil;
    private final MemberPwCheckService memberPwCheckService;
    private final MemberUpdateService memberUpdateService;
    private final UpdateValidator updateValidator;
    private final MemberDeleteService memberDeleteService;
    private final MemberListService memberListService;
    private final EuroService euroService;



    // 마이페이지 들어가기 전 비밀번호 확인
    @GetMapping("/checkpw")
    public String checkPw(Model model){
        model.addAttribute("memberInfo",new MemberInfo());
        model.addAttribute("pageTitle","비밀번호 확인");
        return "member/mypage/checkpw";
    }

    @PostMapping("/checkpw")
    public String checkPw(MemberInfo memberInfo, Model model) {
        String mPassword = memberInfo.getMPassword();
        MemberInfo getMember = memberUtil.getMember();
        Long mNo = getMember.getMNo();

        if(getMember == null){
            return "redirect:/member/login";
        }

        try {
            memberPwCheckService.check(mPassword);
            return "redirect:/member/mypage/info/" + mNo;
        } catch (Exception e) {
            String script = String.format("Swal.fire('%s', '', 'error').then(function(){history.back();})", e.getMessage());
            model.addAttribute("script", script);
            return "script/sweet";
        }
    }

    // 마이페이지 - 회원 정보
    @GetMapping("/info/{mNo}")
    public String myPage(@PathVariable Long mNo, Model model){
        model.addAttribute("pageTitle","마이페이지-회원정보");
        // 다른 회원이 마이페이지에 접근하는 것을 방지
        if(memberUtil.isLogin()) {
            Long no = memberUtil.getMember().getMNo();
            if (!mNo.equals(no)) {
                String script = String.format("Swal.fire('본인 계정만 접근 가능합니다.', '', 'error')" +
                        ".then(function(){location.href='/';})");
                model.addAttribute("script", script);
                return "script/sweet";
            }

            MemberInfo memberInfo = memberUtil.getMember();
            Euro euro = euroService.getMemberEuro(memberInfo.getMNo());
           SignUpForm signUpForm = SignUpForm.builder()
                    .mId(memberInfo.getMId())
                    .mName(memberInfo.getMName())
                    .mNickName(memberInfo.getMNickName())
                    .grade(memberInfo.getGrade())
                    .level(memberInfo.getLevel())
                    .mPhone(memberInfo.getMPhone())
                    .mEmail(memberInfo.getMEmail())
                    .euro(euro)
                    .build();
            model.addAttribute("signUpForm", signUpForm);
            return "member/mypage/info";
        }
        String script = String.format("Swal.fire('로그인 바랍니다.','','error').then(" +
                "function(){location.href='/member/login';})");
        model.addAttribute("script",script);
        return "script/sweet";
    }

    @PostMapping("/info/{mNo}")
    public String myPagePs(@PathVariable Long mNo, @Valid SignUpForm signUpForm,
                           Errors errors, Model model){
        String mPassword = signUpForm.getMPassword();
        String mPhone = signUpForm.getMPhone();

        updateValidator.validate(signUpForm,errors);
        if(errors.hasErrors()){
            MemberInfo memberInfo = memberUtil.getMember();
            Euro euro = euroService.getMemberEuro(mNo);
            signUpForm.setMId(memberInfo.getMId());
            signUpForm.setMName(memberInfo.getMName());
            signUpForm.setMNickName(memberInfo.getMNickName());
            signUpForm.setGrade(memberInfo.getGrade());
            signUpForm.setLevel(memberInfo.getLevel());
            signUpForm.setMPhone(memberInfo.getMPhone());
            signUpForm.setMEmail(memberInfo.getMEmail());
            signUpForm.setEuro(euro);
            return "/member/mypage/info";
        }

        memberUpdateService.update(mNo,mPassword,mPhone);
        String script = String.format("Swal.fire('수정 완료, 재로그인 시 수정된 정보가 반영됩니다. :D','success')" +
                ".then(function(){history.back();})");
        model.addAttribute("script",script);
        return "script/sweet";
    }

    // 마이페이지 - 회원탈퇴
    @GetMapping("/delete/{mNo}")
    public String delete(@PathVariable Long mNo){
        memberDeleteService.delete(mNo);
        return "redirect:/member/logout";
    }

    @GetMapping("/deletepw/{mNo}")
    public String deleteconfirm(@PathVariable Long mNo, @ModelAttribute MemberInfo memberInfo, Model model) {
        model.addAttribute("pageTitle","회원 탈퇴");
        return "member/mypage/checkpw";
    }

    @PostMapping("/deletepw/{mNo}")
    public String deleteconfirmPs(@PathVariable Long mNo, MemberInfo memberInfo, Model model) {
        String mPassword = memberInfo.getMPassword();
        String url = "/member/mypage/delete/" + mNo;
        try {
            if (memberPwCheckService.check(mPassword)) {
                model.addAttribute("confirmUrl", url);
                return "script/sweet";
            }
        } catch (Exception e) {
            String script = String.format("Swal.fire('%s', '', 'error').then(function(){history.back();})", e.getMessage());
            model.addAttribute("script", script);
            return "script/sweet";
        }
        return "redirect:/member/mypage/info/"+mNo;
    }

    // 마이페이지 - 작성한 게시글
    @GetMapping("/mypost/{mNo}")
    public String myPost(@PathVariable Long mNo, @ModelAttribute SearchInfo pageInfo
            , Model model){
        model.addAttribute("pageTitle","마이페이지-작성한 게시글");
        try {
            Long no = memberUtil.getMember().getMNo();
            if (!mNo.equals(no)) {
                String script = String.format("Swal.fire('본인 계정만 접근 가능합니다.', '', 'error')" +
                        ".then(function(){location.href='/';})");
                model.addAttribute("script", script);
                return "script/sweet";
            }

            //페이징 처리된 객체.
            Page<Post> posts = memberListService.getsPostWithPages(pageInfo, mNo);
            // 실제 페이지 안에 담길 내용.
            List<Post> postList = posts.getContent();

            // 페이징 이동하는 기능 구현.
            /*
            getPageable() : 페이지 처리를 위한 정보
            pageNumber : 0(0번째 페이지(1번 의미)의 페이지 선택)
            pageSize : 15(하나의 페이지에 담길 갯수)
            navSize : 정해놓은 페이지 크기
            total page : 전체 페이지 갯수
             */
            // getPageNumber() : Pageable 안에 pageNumber : 현재 페이지(0부터 시작)
            int nowPage = posts.getPageable().getPageNumber() + 1; // 현재 페이지
            int startPage = (nowPage-1) / 10 * 10 + 1; // 첫페이지
            int endPage = Math.min(startPage + 10 - 1, posts.getTotalPages()); // 마지막 페이지

            model.addAttribute("postList", postList);
            model.addAttribute("nowPage", nowPage);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);

        }catch(NullPointerException e){
            String script = String.format("Swal.fire('%s','','error')" +
                    ".then(function(){history.back();})",e.getMessage());
        }
        
        return "member/mypage/mypost";
    }

    @GetMapping("/mybet/{mNo}")
    // 베팅 내역
    public String myBet(@PathVariable Long mNo, Model model){
        model.addAttribute("pageTitle","마이페이지-베팅 내역");
        try {
            Long no = memberUtil.getMember().getMNo();
            if (!mNo.equals(no)) {
                String script = String.format("Swal.fire('본인 계정만 접근 가능합니다.', '', 'error')" +
                        ".then(function(){location.href='/';})");
                model.addAttribute("script", script);
                return "script/sweet";
            }

            List<Bet> betList = memberListService.getsBetList(mNo);
            model.addAttribute("betList",betList);
        }catch(NullPointerException e){
            String script = String.format("Swal.fire('%s','','error')" +
                    ".then(function(){history.back();})",e.getMessage());
        }

        return "member/mypage/mybet";
    }
}
