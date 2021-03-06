package com.salt.batch.jobs;

import com.salt.domain.member.Member;
import com.salt.domain.member.MemberRepository;
import com.salt.domain.member.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j  // log 사용을 위한 lombok Annotation
@AllArgsConstructor  // 생성자 DI를 위한 lombok Annotation
@Configuration
public class unPaidMemberConfig {
    private MemberRepository memberRepository;

    @Bean
    public Job unPaidMemberJob(
            JobBuilderFactory jobBuilderFactory,
            Step unPaidMemberJobStep
    ) {
        log.info("********** This is unPaidMemberJob");
        return jobBuilderFactory.get("unPaidMemberJob")
                .preventRestart()
                .start(unPaidMemberJobStep)
                .build();
    }

    @Bean
    public Step unPaidMemberJobStep(
            StepBuilderFactory stepBuilderFactory
    ) {
        log.info("********** This is unPaidMemberJobStep");
        return stepBuilderFactory.get("unPaidMemberJobStep")
                .<Member, Member> chunk(10)
                .reader(unPaidMemberReader())
                .processor(this.unPaidMemberProcessor())
                .writer(this.unPaidMemberWriter())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<Member> unPaidMemberReader() {
        log.info("********** This is unPaidMemberReader");
        List<Member> activeMembers = memberRepository.findByStatusEquals(MemberStatus.ACTIVE);
        log.info("          - activeMember SIZE : " + activeMembers.size());
        List<Member> unPaidMembers = new ArrayList<>();
        for (Member member : activeMembers) {
            if(member.isUnpaid()) {
                unPaidMembers.add(member);
            }
        }
        log.info("          - unPaidMember SIZE : " + activeMembers.size());
        return new ListItemReader<>(unPaidMembers);
    }

    public ItemProcessor<Member, Member> unPaidMemberProcessor() {
//        return Member::setStatusByunPaid;
        return new ItemProcessor<Member, Member>() {
            @Override
            public Member process(Member member) throws Exception {
                log.info("********** This is unPaidMemberProcessor");
                return member.setStatusByUnPaid();
            }
        };
    }

    public ItemWriter<Member> unPaidMemberWriter() {
        log.info("********** This is unPaidMemberWriter");
        return ((List<? extends Member> memberList) ->
                memberRepository.saveAll(memberList));
    }
}
