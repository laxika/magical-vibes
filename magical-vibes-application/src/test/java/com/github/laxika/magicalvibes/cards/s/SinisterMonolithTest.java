package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinisterMonolith.class, DaggerfangDuo.class})
class SinisterMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat on your turn, each opponent loses 1 life and you gain 1 life")
    void beginningOfCombatDrainsOpponentsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SinisterMonolith());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Paying 2 life and sacrificing it draws two cards")
    void paysLifeSacrificesAndDrawsTwoCards() {
        Permanent monolith = addReadyMonolith();
        harness.setLibrary(player1, List.of(new DaggerfangDuo(), new DaggerfangDuo()));
        harness.setLife(player1, 20);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(monolith);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.assertInGraveyard(player1, "Sinister Monolith");
    }

    @Test
    @DisplayName("Cannot activate its ability outside of a main phase")
    void cannotActivateOutsideMainPhase() {
        addReadyMonolith();
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMonolith() {
        return harness.addToBattlefieldAndReturn(player1, new SinisterMonolith());
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new SinisterMonolith());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent monolith = addReadyMonolith();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(monolith);
        assertThat(monolith.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithAnAbilityOnTheStack() {
        addReadyMonolith();
        Permanent second = addReadyMonolith();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new DaggerfangDuo(), new DaggerfangDuo()));
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second);
        assertThat(second.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    void cannotActivateWithoutEnoughLife() {
        Permanent monolith = addReadyMonolith();
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(monolith);
        assertThat(monolith.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhenTapped() {
        Permanent monolith = addReadyMonolith();
        monolith.tap();
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(monolith);
    }

    @Test
    void canActivateDuringPostcombatMainOnTheTurnItEntered() {
        addReadyMonolith();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DaggerfangDuo(), new DaggerfangDuo()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Sinister Monolith");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
