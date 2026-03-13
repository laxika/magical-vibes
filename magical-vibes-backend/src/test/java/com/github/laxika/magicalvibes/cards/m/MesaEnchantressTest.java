package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MesaEnchantressTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Mesa Enchantress has MayEffect wrapping SpellCastTriggerEffect with enchantment filter")
    void hasCorrectStructure() {
        MesaEnchantress card = new MesaEnchantress();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(trigger.spellFilter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) trigger.spellFilter()).cardType()).isEqualTo(CardType.ENCHANTMENT);
    }

    // ===== Trigger fires on enchantment cast =====

    @Test
    @DisplayName("Casting an enchantment spell triggers may ability prompt")
    void enchantmentCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept: draws a card =====

    @Test
    @DisplayName("Accepting draws a card")
    void acceptDrawsACard() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Mesa Enchantress"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Card was drawn
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not draw")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castEnchantment(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Mesa Enchantress"));

        // Deck size unchanged (no draw happened)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    // ===== Non-enchantment does not trigger =====

    @Test
    @DisplayName("Non-enchantment spell does not trigger Mesa Enchantress")
    void nonEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's enchantment does not trigger =====

    @Test
    @DisplayName("Opponent casting enchantment does not trigger Mesa Enchantress")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MesaEnchantress());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new HonorOfThePure()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    // ===== Helpers =====

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
