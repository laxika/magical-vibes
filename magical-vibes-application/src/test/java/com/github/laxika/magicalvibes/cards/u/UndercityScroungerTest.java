package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercityScrounger.class, GrizzlyBears.class, Shock.class})
class UndercityScroungerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without a creature having died this turn")
    void cannotActivateWithoutMorbid() {
        Permanent scrounger = addReadyScrounger();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scrounger), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Morbid");
    }

    @Test
    @DisplayName("Creates a Treasure after a creature dies")
    void createsTreasureAfterCreatureDies() {
        Permanent scrounger = addReadyScrounger();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        int scroungerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scrounger);
        harness.activateAbility(player1, scroungerIndex, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(scrounger.isTapped()).isTrue();
    }

    private Permanent addReadyScrounger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new UndercityScrounger());
        scrounger.setSummoningSick(false);
        return scrounger;
    }
}
