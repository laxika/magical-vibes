package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        harness.castAndResolveInstant(player1, 0, bears.getId());

        int scroungerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scrounger);
        harness.activateAbility(player1, scroungerIndex, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(scrounger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature you control dying also enables the ability")
    void createsTreasureAfterOwnCreatureDies() {
        Permanent scrounger = addReadyScrounger();
        killBears(player1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scrounger), null, null);

        assertThat(scrounger.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness prevents activation even after a creature dies")
    void cannotActivateWhileSummoningSick() {
        Permanent scrounger = addReadyScrounger();
        scrounger.setSummoningSick(true);
        killBears(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scrounger), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scrounger.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("An untapped Scrounger may activate again after the same creature death")
    void deathDoesNotGetConsumedByActivation() {
        Permanent scrounger = addReadyScrounger();
        killBears(player2);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(scrounger);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
        scrounger.untap();
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(scrounger.isTapped()).isTrue();
    }

    private void killBears(Player controller) {
        Permanent bears = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.playerGraveyards.get(controller.getId())).contains(bears.getCard());
    }

    private Permanent addReadyScrounger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        return addCreatureReady(player1, new UndercityScrounger());
    }
}
