package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Coercion;
import com.github.laxika.magicalvibes.cards.s.StandingTroops;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecurringNightmare.class, StandingTroops.class, Rootwalla.class, Coercion.class})
class RecurringNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, returns itself to hand, and reanimates the target")
    void sacrificesBouncesAndReanimates() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        harness.addToBattlefield(player1, new StandingTroops());
        Card returned = new Rootwalla();
        harness.setGraveyard(player1, List.of(returned));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(returned.getId()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Recurring Nightmare")).isZero();
        harness.assertInHand(player1, "Recurring Nightmare");
        harness.assertNotOnBattlefield(player1, "Standing Troops");
        harness.assertInGraveyard(player1, "Standing Troops");
        assertThat(countPermanents(player1, "Rootwalla")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Rootwalla");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        harness.addToBattlefield(player1, new StandingTroops());
        Card noncreature = new Coercion();
        harness.setGraveyard(player1, List.of(noncreature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Recurring Nightmare");
        harness.assertOnBattlefield(player1, "Standing Troops");
        harness.assertInGraveyard(player1, "Coercion");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        Card returned = new Rootwalla();
        harness.setGraveyard(player1, List.of(returned));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(returned.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Recurring Nightmare");
        harness.assertInGraveyard(player1, "Rootwalla");
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        harness.addToBattlefield(player1, new StandingTroops());
        Card returned = new Rootwalla();
        harness.setGraveyard(player2, List.of(returned));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(returned.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Recurring Nightmare");
        harness.assertOnBattlefield(player1, "Standing Troops");
        harness.assertInGraveyard(player2, "Rootwalla");
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        harness.addToBattlefield(player1, new StandingTroops());
        Card returned = new Rootwalla();
        harness.setGraveyard(player1, List.of(returned));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(returned.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }
}
