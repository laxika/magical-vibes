package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShrineOfPiercingVisionTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Shrine has upkeep trigger, blue spell cast trigger, and activated ability")
    void hasCorrectAbilityStructure() {
        ShrineOfPiercingVision card = new ShrineOfPiercingVision();

        // Upkeep triggered ability (mandatory charge counter)
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Spell cast trigger (blue spell -> charge counter)
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        var spellTrigger = card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(spellTrigger).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) spellTrigger;
        assertThat(trigger.spellFilter()).isInstanceOf(CardColorPredicate.class);
        assertThat(((CardColorPredicate) trigger.spellFilter()).color()).isEqualTo(CardColor.BLUE);
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Activated ability (tap + sacrifice -> look at top X)
        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect.class);
    }

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("Upkeep trigger adds a charge counter (mandatory)")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // move to upkeep, trigger fires
        harness.passBothPriorities(); // resolve PutChargeCounterOnSelfEffect

        assertThat(shrine.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's upkeep does not add a charge counter")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shrine.getChargeCounters()).isEqualTo(0);
    }

    // ===== Blue spell cast trigger =====

    @Test
    @DisplayName("Casting a blue spell adds a charge counter")
    void castingBlueSpellAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        // Telling Time is a blue instant
        com.github.laxika.magicalvibes.cards.t.TellingTime tellingTime =
                new com.github.laxika.magicalvibes.cards.t.TellingTime();
        harness.setHand(player1, List.of(tellingTime));
        harness.castInstant(player1, 0);

        // Spell cast trigger should put charge counter on shrine
        harness.passBothPriorities(); // resolve charge counter trigger
        assertThat(shrine.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-blue spell does not add a charge counter")
    void castingNonBlueSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.castInstant(player1, 0, player2.getId());

        // No charge counter trigger should fire — resolve Shock
        harness.passBothPriorities();

        assertThat(shrine.getChargeCounters()).isEqualTo(0);
    }

    // ===== Activated ability: Tap + sacrifice to look at top X =====

    @Test
    @DisplayName("Sacrificing with charge counters enters hand/top/bottom choice state")
    void sacrificeEntersHandTopBottomChoiceState() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability from stack

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
        assertThat(gd.interaction.libraryView().handTopBottomPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.libraryView().handTopBottomCards()).hasSize(3);
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and rest on bottom")
    void choosingCardPutsInHandRestOnBottom() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(3);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        int originalDeckSize = deck.size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose: card 1 to hand, card 0 to top, card 2 to bottom
        gs.handleHandTopBottomChosen(gd, player1, 1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(top1);
        assertThat(deck.get(0)).isSameAs(top0);
        assertThat(deck.get(deck.size() - 1)).isSameAs(top2);
        assertThat(deck).hasSize(originalDeckSize - 1);
    }

    @Test
    @DisplayName("Shrine is sacrificed as a cost (goes to graveyard immediately)")
    void shrineIsSacrificedAsCost() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(2);

        harness.activateAbility(player1, 0, null, null);

        // Shrine should be in graveyard immediately (sacrifice is a cost)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shrine of Piercing Vision"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shrine of Piercing Vision"));
    }

    @Test
    @DisplayName("Sacrificing with zero counters does nothing")
    void sacrificeWithZeroCountersDoesNothing() {
        Permanent shrine = addReadyShrine(player1);
        // No charge counters

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        // No cards should be added to hand
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no charge counters"));
    }

    @Test
    @DisplayName("Sacrificing with 1 counter auto-puts that card into hand")
    void sacrificeWithOneCounterAutoChooses() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Single card goes directly to hand — no choice needed
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("Activated ability requires tap — tapped shrine cannot activate")
    void activatedAbilityRequiresTap() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(2);
        shrine.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability goes on the stack (not a mana ability)")
    void abilityGoesOnStack() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(2);

        int stackSizeBefore = gd.stack.size();

        harness.activateAbility(player1, 0, null, null);

        // Should add an entry to the stack (not a mana ability)
        assertThat(gd.stack.size()).isEqualTo(stackSizeBefore + 1);
    }

    @Test
    @DisplayName("More charge counters than cards in library uses available cards")
    void moreCountersThanCardsInLibrary() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(100);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        int deckSize = deck.size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should use all available cards (capped at deck size)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
        assertThat(gd.interaction.libraryView().handTopBottomCards()).hasSize(deckSize);
    }

    // ===== Helper methods =====

    private Permanent addReadyShrine(Player player) {
        ShrineOfPiercingVision card = new ShrineOfPiercingVision();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
