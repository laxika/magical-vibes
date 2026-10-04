package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Gastal Thrillroller")
@CardUsed({GastalThrillroller.class, GrizzlyBears.class, Forest.class})
class GastalThrillrollerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB animates it until end of turn")
    void etbAnimatesUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GastalThrillroller()));
        addThrillrollerMana();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent thrillroller = findPermanent(player1, "Gastal Thrillroller");
        assertThat(gqs.isCreature(gd, thrillroller)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, thrillroller)).isFalse();
    }

    @Test
    @DisplayName("Crew 2 animates it and taps the crew")
    void crewAnimatesAndTapsCrew() {
        Permanent thrillroller = addCreatureReady(player1, new GastalThrillroller());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, thrillroller)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns from the graveyard after discarding and gains a finality counter")
    void returnsFromGraveyardWithFinalityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GastalThrillroller card = new GastalThrillroller();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Forest()));
        addThrillrollerMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        Permanent thrillroller = findPermanent(player1, "Gastal Thrillroller");
        assertThat(thrillroller.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A finality counter exiles it instead of putting it into a graveyard")
    void finalityCounterExilesItInsteadOfDying() {
        Permanent thrillroller = addCreatureReady(player1, new GastalThrillroller());
        thrillroller.setCounterCount(CounterType.FINALITY, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, thrillroller));

        harness.assertNotOnBattlefield(player1, "Gastal Thrillroller");
        harness.assertNotInGraveyard(player1, "Gastal Thrillroller");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gastal Thrillroller"));
    }

    private void addThrillrollerMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Crew animation ends at the end of the turn")
    void crewAnimationEndsAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent thrillroller = addCreatureReady(player1, new GastalThrillroller());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, thrillroller)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, thrillroller)).isFalse();
    }

    @Test
    @DisplayName("It can attack on the turn it enters without being crewed")
    void canAttackImmediatelyWithoutCrew() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GastalThrillroller()));
        addThrillrollerMana();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Only the activated copy returns, even if another copy is discarded")
    void returnsOnlyActivatedCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GastalThrillroller source = new GastalThrillroller();
        GastalThrillroller other = new GastalThrillroller();
        GastalThrillroller discarded = new GastalThrillroller();
        harness.setGraveyard(player1, List.of(source, other));
        harness.setHand(player1, List.of(discarded));
        addThrillrollerMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Gastal Thrillroller")).isEqualTo(1);
        assertThat(findPermanent(player1, "Gastal Thrillroller").getCard().getId()).isEqualTo(source.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, discarded);
        Permanent returned = findPermanent(player1, "Gastal Thrillroller");
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated during combat")
    void cannotReturnDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new GastalThrillroller()));
        harness.setHand(player1, List.of(new Forest()));
        addThrillrollerMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Gastal Thrillroller");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated on the opponent's turn")
    void cannotReturnOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new GastalThrillroller()));
        harness.setHand(player1, List.of(new Forest()));
        addThrillrollerMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Gastal Thrillroller");
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated while another ability is on the stack")
    void cannotReturnWithNonemptyStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player1, new GastalThrillroller());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GastalThrillroller()));
        harness.setHand(player1, List.of(new Forest()));
        addThrillrollerMana();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isNotEmpty();
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Gastal Thrillroller");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Returning requires a card to discard")
    void cannotReturnWithoutDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new GastalThrillroller()));
        harness.setHand(player1, List.of());
        addThrillrollerMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Gastal Thrillroller");
    }
}
