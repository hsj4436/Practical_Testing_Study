package com.example.cafekiosk.learning;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class GuavaLearningTest {

    @Test
    @DisplayName("주어진 개수만큼 List를 파티셔닝한다.")
    void partitionLearningTest1() {
        // given
        List<Integer> integers = List.of(1, 2, 3, 4, 5, 6);

        // when
        List<List<Integer>> partition = Lists.partition(integers, 3);

        // then
        assertThat(partition).hasSize(2)
            .isEqualTo(List.of(
                List.of(1, 2, 3),
                List.of(4, 5, 6)
            ));
    }

    @Test
    @DisplayName("주어진 개수만큼 List를 파티셔닝한다.")
    void partitionLearningTest2() {
        // given
        List<Integer> integers = List.of(1, 2, 3, 4, 5, 6);

        // when
        List<List<Integer>> partition = Lists.partition(integers, 4);

        // then
        assertThat(partition).hasSize(2)
            .isEqualTo(List.of(
                List.of(1, 2, 3, 4),
                List.of(5, 6)
            ));
    }

    @Test
    @DisplayName("멀티맵 기능 확인")
    void multiMapLearningTest1() {
        // given
        Multimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("커피", "아메리카노");
        multimap.put("커피", "카페 라떼");
        multimap.put("커피", "바닐라 라떼");
        multimap.put("베이커리", "크루아상");
        multimap.put("베이커리", "모닝빵");

        // when
        Collection<String> strings = multimap.get("커피");

        // then
        assertThat(strings).hasSize(3)
            .isEqualTo(List.of("아메리카노", "카페 라떼", "바닐라 라떼"));
    }

    @TestFactory
    @DisplayName("멀티맵 기능 확인")
    Collection<DynamicTest> multiMapLearningTest2() {
        // given
        Multimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("커피", "아메리카노");
        multimap.put("커피", "카페 라떼");
        multimap.put("커피", "바닐라 라떼");
        multimap.put("베이커리", "크루아상");
        multimap.put("베이커리", "모닝빵");

        return List.of(
            DynamicTest.dynamicTest("1개 value 삭제", () -> {
                // when
                multimap.remove("커피", "카페 라떼");

                // then
                Collection<String> strings = multimap.get("커피");
                assertThat(strings).hasSize(2)
                    .isEqualTo(List.of("아메리카노", "바닐라 라떼"));
            }),
            DynamicTest.dynamicTest("1개 key 삭제", () -> {
                // when
                multimap.removeAll("커피");

                // then
                Collection<String> strings = multimap.get("커피");
                assertThat(strings).isEmpty();
            })
        );
    }
}
