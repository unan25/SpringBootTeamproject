package com.springboot.shootformoney.common.interceptors;

import com.springboot.shootformoney.member.dto.MemberInfo;
import com.springboot.shootformoney.member.entity.Euro;
import com.springboot.shootformoney.member.entity.LoginData;
import com.springboot.shootformoney.member.entity.Member;
import com.springboot.shootformoney.member.repository.EuroRepository;
import com.springboot.shootformoney.member.repository.MemberRepository;
import com.springboot.shootformoney.member.utils.LevelGradeUtil;
import com.springboot.shootformoney.member.utils.MemberUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.text.NumberFormat;
import java.time.LocalDateTime;

@Component("memberUpdate")
@RequiredArgsConstructor
public class MemberUpdateInterceptor implements HandlerInterceptor {

    private final LevelGradeUtil levelRankUtil;
    private final MemberUtil memberUtil;
    private final MemberRepository memberRepository;
    private final EuroRepository euroRepository;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        if(memberUtil.isLogin()){
            MemberInfo memberInfo = memberUtil.getMember();
            Long mNo = memberInfo.getMNo();
            String nickname = memberInfo.getMNickName();
            Member member = memberRepository.findBymNickName(nickname);
            Integer level = member.getMLevel();
            Euro euro = euroRepository.findBymNo(mNo);
            String formattedEuro = String.format("%,d",euro.getValue());
            req.setAttribute("mNo",mNo);
            req.setAttribute("formattedEuro",formattedEuro);
            req.setAttribute("level",level);

            member = levelRankUtil.levelUp(mNo);
            member = levelRankUtil.gradeUp(member);

            LoginData loginData = member.getLoginData();
            if(loginData == null){
                loginData = new LoginData();
                loginData.setLoginDate(LocalDateTime.now());
                member.setLoginData(loginData);
            }else{
                loginData.setLoginDate(LocalDateTime.now());
            }
            memberRepository.saveAndFlush(member);
        }
        return true;
    }
}
