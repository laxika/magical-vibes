package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkSpy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeeprootWatersTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Deeproot Waters has SpellCastTriggerEffect with Merfolk filter and token creation")
    void hasCorrectStructure() {
        DeeprootWaters card = new DeeprootWaters();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.manaCost()).isNull();
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect = (CreateTokenEffect) trigger.resolvedEffects().getFirst();
        assertThat(tokenEffect.tokenName()).isEqualTo("Merfolk");
        assertThat(tokenEffect.power()).isEqualTo(1);
        assertThat(tokenEffect.toughness()).isEqualTo(1);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.BLUE);
        assertThat(tokenEffect.subtypes()).contains(CardSubtype.MERFOLK);
        assertThat(tokenEffect.keywords()).contains(Keyword.HEXPROOF);
    }

    // ===== Trigger fires on Merfolk cast =====

    @Test
    @DisplayName("Casting a Merfolk spell triggers token creation")
    void merfolkCastTriggersTokenCreation() {
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Deeproot Waters"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // A Merfolk token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Merfolk")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == CardColor.BLUE
                        && p.getCard().getSubtypes().contains(CardSubtype.MERFOLK)
                        && p.getCard().getKeywords().contains(Keyword.HEXPROOF)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    // ===== Non-Merfolk does not trigger =====

    @Test
    @DisplayName("Non-Merfolk spell does not trigger Deeproot Waters")
    void nonMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Stack should only have the creature spell, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's Merfolk does not trigger =====

    @Test
    @DisplayName("Opponent casting Merfolk does not trigger Deeproot Waters")
    void opponentMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeeprootWaters());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new MerfolkSpy()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        // No triggered ability from Deeproot Waters
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Deeproot Waters"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Multiple Merfolk casts create multiple tokens =====

    @Test
    @DisplayName("Casting two Merfolk spells creates two tokens")
    void multipleMerfolkCastsCreateMultipleTokens() {
        harness.addToBattlefield(player1, new DeeprootWaters());
        harness.setHand(player1, List.of(new MerfolkSpy(), new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Cast first Merfolk
        harness.castCreature(player1, 0);
        // Resolve triggered ability
        harness.passBothPriorities();
        // Resolve the creature spell
        harness.passBothPriorities();

        // Cast second Merfolk
        harness.castCreature(player1, 0);
        // Resolve triggered ability
        harness.passBothPriorities();
        // Resolve the creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Merfolk") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }
}
