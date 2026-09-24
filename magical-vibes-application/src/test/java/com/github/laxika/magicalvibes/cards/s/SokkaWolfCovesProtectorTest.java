package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SokkaWolfCovesProtector.class)
class SokkaWolfCovesProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Sokka untapped when he attacks")
    void vigilanceKeepsSokkaUntappedWhenAttacking() {
        Permanent sokka = addCreatureReady(player1, new SokkaWolfCovesProtector());

        declareAttackers(List.of(0));

        assertThat(sokka.isTapped()).isFalse();
    }
}
