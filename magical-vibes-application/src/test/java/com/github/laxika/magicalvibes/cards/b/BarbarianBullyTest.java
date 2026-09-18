package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianBully.class, SuntailHawk.class})
class BarbarianBullyTest extends BaseCardTest {

    @Test
    void activationDiscardsOneCardAsCost() {
        BarbarianBully discarded = new BarbarianBully();
        activateWithHand(discarded);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
    }

    @Test
    void allPlayersDeclineAndBullyGetsBoosted() {
        Permanent bully = activateWithHand(new BarbarianBully());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bully.getPowerModifier()).isEqualTo(2);
        assertThat(bully.getToughnessModifier()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void firstPlayerToAcceptTakesDamageAndPreventsBoost() {
        Permanent bully = activateWithHand(new BarbarianBully());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(bully.getPowerModifier()).isZero();
        assertThat(bully.getToughnessModifier()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canActivateOnlyOnceEachTurn() {
        activateWithHand(new BarbarianBully());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.setHand(player1, List.of(new BarbarianBully()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefieldAndReturn(player1, new BarbarianBully());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards to discard at random");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent bully = activateWithHand(new BarbarianBully());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(bully.getPowerModifier()).isEqualTo(2);
        assertThat(bully.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bully.getPowerModifier()).isZero();
        assertThat(bully.getToughnessModifier()).isZero();
    }

    @Test
    void canActivateAgainOnLaterTurn() {
        Permanent bully = activateWithHand(new BarbarianBully());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarbarianBully()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bully.getPowerModifier()).isEqualTo(2);
        assertThat(bully.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent activateWithHand(BarbarianBully cardInHand) {
        Permanent bully = harness.addToBattlefieldAndReturn(player1, new BarbarianBully());
        harness.setHand(player1, List.of(cardInHand));
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return bully;
    }

    @Test
    void activationDiscardsOneCardAtRandomAsCost() {
        activateWithHandForJudReview(new SuntailHawk());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Suntail Hawk");

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
    }

    @Test
    void activePlayerCanAcceptDamage() {
        Permanent bully = activateWithHandForJudReview(new SuntailHawk());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(bully.getPowerModifier()).isZero();
        assertThat(bully.getToughnessModifier()).isZero();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent activateWithHandForJudReview(SuntailHawk cardInHand) {
        Permanent bully = harness.addToBattlefieldAndReturn(player1, new BarbarianBully());
        harness.setHand(player1, List.of(cardInHand));
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return bully;
    }
}
