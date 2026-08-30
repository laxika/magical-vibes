package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardenOfTheChained.class, AirElemental.class, HillGiant.class})
class WardenOfTheChainedTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when it is the only creature with power 4 or greater")
    void cannotAttackWithoutAnotherPowerFourCreature() {
        addCreatureReady(player1, new WardenOfTheChained());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when the other creature has power 3")
    void cannotAttackWithOnlyPowerThreeCreature() {
        addCreatureReady(player1, new WardenOfTheChained());
        addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controlling another creature with power 4")
    void canAttackWithAnotherPowerFourCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WardenOfTheChained());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }
}
