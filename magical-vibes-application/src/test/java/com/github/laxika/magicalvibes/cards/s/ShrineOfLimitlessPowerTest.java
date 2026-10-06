package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.d.DementiaBat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ShrineOfLimitlessPower.class, DementiaBat.class, Forest.class, GrizzlyBears.class, Peek.class})
class ShrineOfLimitlessPowerTest extends BaseCardTest {


    @Test
    @DisplayName("Upkeep trigger puts a charge counter on Shrine (mandatory)")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate charge counters")
    void multipleUpkeepsAccumulateCounters() {
        Permanent shrine = addReadyShrine(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's upkeep does not trigger Shrine")
    void opponentUpkeepDoesNotTrigger() {
        Permanent shrine = addReadyShrine(player1);

        advanceToUpkeep(player2);
        // No trigger should be on the stack for the Shrine
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }


    @Test
    @DisplayName("Casting a black spell puts a charge counter on Shrine")
    void castingBlackSpellAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player1, List.of(new DementiaBat()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        // Triggered ability should be on the stack
        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Shrine of Limitless Power"))
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve charge counter trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-black spell does not add a charge counter")
    void castingNonBlackSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent casting a black spell does not trigger Shrine")
    void opponentCastingBlackSpellDoesNotTrigger() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player2, List.of(new DementiaBat()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }


    @Test
    @DisplayName("Activating ability causes target player to discard cards equal to charge counters")
    void activateDiscardsEqualToChargeCounters() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(3);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activating with 0 charge counters discards nothing")
    void activateWithZeroCountersDiscardsNothing() {
        Permanent shrine = addReadyShrine(player1);
        // No charge counters
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Shrine is sacrificed as part of the cost")
    void shrineIsSacrificedAsCost() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Shrine should be in graveyard immediately (sacrifice is a cost)
        harness.assertNotOnBattlefield(player1, "Shrine of Limitless Power");
        harness.assertInGraveyard(player1, "Shrine of Limitless Power");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyShrine(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // need 4

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability requires tap")
    void activatedAbilityRequiresTap() {
        Permanent shrine = addReadyShrine(player1);
        shrine.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target with empty hand results in no discard prompt")
    void targetWithEmptyHandNoPrompt() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player2, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Charge counters from upkeep and cast trigger both count for discard")
    void chargeCountersFromBothSourcesCountForDiscard() {
        Permanent shrine = addReadyShrine(player1);

        // Get 1 counter from upkeep
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Get 1 counter from black spell
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DementiaBat()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve charge counter trigger
        harness.passBothPriorities(); // resolve creature spell
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        // Now activate — should discard 2 cards
        shrine.untap(); // untap since it was never tapped, but just in case
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }


    @Test
    @DisplayName("Controller can target themselves and choose which card to discard")
    void canTargetControllerAndChooseDiscard() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new Forest(), new DementiaBat()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player1, "Dementia Bat");
        harness.assertInGraveyard(player1, "Dementia Bat");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Target discards their entire hand when counters exceed hand size")
    void countersExceedingHandSizeDiscardAllAvailableCards() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 5);
        harness.setHand(player2, List.of(new Forest(), new DementiaBat()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Dementia Bat");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing before an upkeep trigger resolves uses only existing counters")
    void pendingUpkeepCounterDoesNotCountForDiscard() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player2, List.of(new Forest(), new DementiaBat()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Shrine of Limitless Power");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private Permanent addReadyShrine(Player player) {
        return addCreatureReady(player, new ShrineOfLimitlessPower());
    }
}
