package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkovPurifier.class, DoomedDissenter.class})
class MarkovPurifierTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {2} to draw a card at your end step after gaining life")
    void paysToDrawAfterGainingLife() {
        harness.addToBattlefield(player1, new MarkovPurifier());
        harness.setHand(player1, List.of());
        DoomedDissenter card = new DoomedDissenter();
        harness.setLibrary(player1, List.of(card));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{2}");
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Declining the payment does not draw a card")
    void decliningPaymentDoesNotDraw() {
        harness.addToBattlefield(player1, new MarkovPurifier());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DoomedDissenter()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at your end step without gaining life")
    void doesNotTriggerWithoutGainingLife() {
        harness.addToBattlefield(player1, new MarkovPurifier());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DoomedDissenter()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Life gained by an opponent does not qualify")
    void opponentLifeGainDoesNotQualify() {
        harness.addToBattlefield(player1, new MarkovPurifier());
        gd.lifeGainedThisTurn.put(player2.getId(), 3);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new MarkovPurifier());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Gaining more life allows only one payment and one draw")
    void moreLifeStillDrawsOnlyOneCard() {
        harness.addToBattlefield(player1, new MarkovPurifier());
        harness.setHand(player1, List.of());
        DoomedDissenter first = new DoomedDissenter();
        DoomedDissenter second = new DoomedDissenter();
        harness.setLibrary(player1, List.of(first, second));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Cannot draw when the full payment is unavailable")
    void insufficientManaDoesNotDraw() {
        harness.addToBattlefield(player1, new MarkovPurifier());
        harness.setHand(player1, List.of());
        DoomedDissenter card = new DoomedDissenter();
        harness.setLibrary(player1, List.of(card));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lifelink combat damage enables the end-step draw")
    void lifelinkEnablesEndStepDraw() {
        harness.addToBattlefieldAndReturn(player1, new MarkovPurifier()).setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        DoomedDissenter card = new DoomedDissenter();
        harness.setLibrary(player1, List.of(card));

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
