package likelion14th.lte.statistic.dto;

import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatisticResponse {

    private int streak;
    private int monthPercent;
    private WeekEnum mostTodoWeek;

    public static StatisticResponse from(Statistic statistic) {
        return StatisticResponse.builder()
                .streak(statistic.getStreak())
                .monthPercent(statistic.getMonthPercent())
                .mostTodoWeek(statistic.getMostTodoWeek())
                .build();
    }
}
