package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MishrasWarMachine.class, GrizzlyBears.class, GrayOgre.class})
class MishrasWarMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the discard deals 3 damage to its controller and taps it")
    void declineDealsDamageAndTaps() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Mishra's War Machine").isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
        // Card was not discarded
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting discards a card with no damage and no tap")
    void acceptDiscardsCardNoPenalty() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Mishra's War Machine").isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With no cards in hand, the penalty applies immediately without a prompt")
    void noCardsAppliesPenaltyImmediately() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → penalty (no card to discard)

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Mishra's War Machine").isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does not tap when all penalty damage is prevented")
    void preventedPenaltyDamageDoesNotTapSource() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>());
        gd.globalDamagePreventionShield = 3;
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mishra's War Machine").isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.globalDamagePreventionShield).isZero();
    }

    @Test
    @DisplayName("Can band with one non-banding attacker")
    void canBandWithOneNonBandingAttacker() {
        Permanent machine = addCreatureReady(player1, new MishrasWarMachine());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(machine.getBandId()).isNotNull();
        assertThat(machine.getBandId()).isEqualTo(nonBander.getBandId());
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mishra's War Machine").isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
