package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DementiaBat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShrineOfLimitlessPowerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has upkeep trigger, black spell cast trigger, and activated ability")
    void hasCorrectStructure() {
        ShrineOfLimitlessPower card = new ShrineOfLimitlessPower();

        // Upkeep trigger — mandatory charge counter
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).singleElement()
                .isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Cast trigger — black spell adds charge counter
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).singleElement()
                .isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect castTrigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(castTrigger.spellFilter()).isInstanceOf(CardColorPredicate.class);
        assertThat(((CardColorPredicate) castTrigger.spellFilter()).color()).isEqualTo(CardColor.BLACK);
        assertThat(castTrigger.resolvedEffects()).singleElement().isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Activated ability — {4}, T, sacrifice: target player discards by charge counters
        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{4}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(TargetPlayerDiscardsByChargeCountersEffect.class);
        assertThat(ability.isNeedsTarget()).isTrue();
    }

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("Upkeep trigger puts a charge counter on Shrine (mandatory)")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(shrine.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate charge counters")
    void multipleUpkeepsAccumulateCounters() {
        Permanent shrine = addReadyShrine(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(shrine.getChargeCounters()).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(shrine.getChargeCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's upkeep does not trigger Shrine")
    void opponentUpkeepDoesNotTrigger() {
        Permanent shrine = addReadyShrine(player1);

        advanceToUpkeep(player2);
        // No trigger should be on the stack for the Shrine
        assertThat(shrine.getChargeCounters()).isEqualTo(0);
    }

    // ===== Black spell cast trigger =====

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

        assertThat(shrine.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-black spell does not add a charge counter")
    void castingNonBlackSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(shrine.getChargeCounters()).isEqualTo(0);
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

        assertThat(shrine.getChargeCounters()).isEqualTo(0);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability causes target player to discard cards equal to charge counters")
    void activateDiscardsEqualToChargeCounters() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(3);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(3);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
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

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Shrine is sacrificed as part of the cost")
    void shrineIsSacrificedAsCost() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Shrine should be in graveyard immediately (sacrifice is a cost)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shrine of Limitless Power"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shrine of Limitless Power"));
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
        shrine.setChargeCounters(3);
        harness.setHand(player2, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Charge counters from upkeep and cast trigger both count for discard")
    void chargeCountersFromBothSourcesCountForDiscard() {
        Permanent shrine = addReadyShrine(player1);

        // Get 1 counter from upkeep
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger
        assertThat(shrine.getChargeCounters()).isEqualTo(1);

        // Get 1 counter from black spell
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DementiaBat()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve charge counter trigger
        harness.passBothPriorities(); // resolve creature spell
        assertThat(shrine.getChargeCounters()).isEqualTo(2);

        // Now activate — should discard 2 cards
        shrine.untap(); // untap since it was never tapped, but just in case
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    // ===== Helper methods =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyShrine(Player player) {
        ShrineOfLimitlessPower card = new ShrineOfLimitlessPower();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
