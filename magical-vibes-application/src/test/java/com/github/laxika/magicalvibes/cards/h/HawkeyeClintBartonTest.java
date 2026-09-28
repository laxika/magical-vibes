package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HawkeyeClintBarton.class)
class HawkeyeClintBartonTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Hawkeye untapped when he attacks")
    void vigilanceKeepsHawkeyeUntapped() {
        Permanent hawkeye = addCreatureReady(player1, new HawkeyeClintBarton());

        declareAttackers(List.of(0));

        assertThat(hawkeye.isTapped()).isFalse();
    }
}
