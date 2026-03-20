package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentLeavesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CuratorsWardTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Curator's Ward has static hexproof grant and LTB conditional trigger")
    void hasExpectedEffects() {
        CuratorsWard card = new CuratorsWard();

        assertThat(card.isAura()).isTrue();

        // Static hexproof
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(grant.keyword()).isEqualTo(Keyword.HEXPROOF);
        assertThat(grant.scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);

        // LTB conditional trigger
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD).getFirst())
                .isInstanceOf(EnchantedPermanentLeavesConditionalEffect.class);
        EnchantedPermanentLeavesConditionalEffect trigger =
                (EnchantedPermanentLeavesConditionalEffect) card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD).getFirst();
        assertThat(trigger.permanentFilter()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) trigger.resolvedEffects().getFirst()).amount()).isEqualTo(2);
    }

    // ===== Hexproof grant =====

    @Test
    @DisplayName("Enchanted permanent gains hexproof")
    void enchantedPermanentHasHexproof() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Permanent loses hexproof when Curator's Ward is removed")
    void permanentLosesHexproofWhenWardRemoved() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        // Remove the ward aura
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Curator's Ward"));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    // ===== LTB trigger — historic permanent destroyed =====

    @Test
    @DisplayName("Draw 2 when enchanted legendary creature is destroyed")
    void drawsWhenLegendaryCreatureDestroyed() {
        // Use GrizzlyBears with legendary supertype manually set to avoid trigger interference
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent creature = addCreatureWithWard(player1, player1, legendaryBears);

        // Controller destroys own creature (hexproof doesn't prevent self-targeting)
        setupAndCastDoomBlade(player1, creature);
        // After setHand+cast: hand has 0 cards. DoomBlade on stack.
        harness.passBothPriorities(); // resolve Doom Blade — creature destroyed, LTB trigger on stack
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // Drew 2 cards from trigger
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draw 2 when enchanted artifact is destroyed")
    void drawsWhenArtifactDestroyed() {
        Permanent artifact = addArtifactWithWard(player1, player1);

        // Controller destroys own artifact (hexproof doesn't prevent self-targeting)
        setupAndCastShatter(player1, artifact);
        harness.passBothPriorities(); // resolve Shatter — artifact destroyed, LTB trigger on stack
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // Drew 2 cards from trigger
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
    }

    // ===== LTB trigger — non-historic permanent destroyed =====

    @Test
    @DisplayName("No draw when non-historic creature is destroyed")
    void noDrawWhenNonHistoricDestroyed() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());

        setupAndCastDoomBlade(player1, creature);
        harness.passBothPriorities(); // resolve Doom Blade — creature destroyed, no LTB trigger
        // No second pass needed — no trigger on stack

        // No cards drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(0);
    }

    // ===== LTB trigger — historic permanent bounced =====

    @Test
    @DisplayName("Draw 2 when enchanted legendary creature is bounced")
    void drawsWhenLegendaryBounced() {
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent creature = addCreatureWithWard(player1, player1, legendaryBears);

        setupAndCastDisperse(player1, creature);
        harness.passBothPriorities(); // resolve Disperse — creature bounced to hand, LTB trigger on stack
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // 1 (Bears returned to hand) + 2 (drew from trigger) = 3
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(3);
    }

    @Test
    @DisplayName("No draw when non-historic creature is bounced")
    void noDrawWhenNonHistoricBounced() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());

        setupAndCastDisperse(player1, creature);
        harness.passBothPriorities(); // resolve Disperse — creature bounced to hand, no trigger

        // 1 (Bears returned to hand) only
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(1);
    }

    // ===== Aura controller draws (not permanent controller) =====

    @Test
    @DisplayName("Aura controller draws cards, not enchanted permanent's controller")
    void auraControllerDraws() {
        // Player 1 controls the aura on Player 2's legendary creature
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent creature = addCreatureWithWard(player2, player1, legendaryBears);

        int hand1Before = gd.playerHands.get(player1.getId()).size();

        // Player 2 (creature controller) can target own creature through hexproof
        setupAndCastDoomBlade(player2, creature);
        harness.passBothPriorities(); // resolve Doom Blade
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // Aura controller (player1) draws 2 — player1's hand wasn't touched by setHand
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(hand1Before + 2);
        // Permanent controller (player2) doesn't draw from trigger — hand is 0 after casting DoomBlade
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addCreatureWithWard(Player creatureController, Player auraController, Card creatureCard) {
        harness.addToBattlefield(creatureController, creatureCard);
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).stream()
                .filter(p -> p.getCard().getName().equals(creatureCard.getName()))
                .findFirst().orElseThrow();

        CuratorsWard wardCard = new CuratorsWard();
        Permanent ward = new Permanent(wardCard);
        ward.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(ward);

        return creature;
    }

    private Permanent addArtifactWithWard(Player artifactController, Player auraController) {
        harness.addToBattlefield(artifactController, new Spellbook());
        Permanent artifact = gd.playerBattlefields.get(artifactController.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst().orElseThrow();

        CuratorsWard wardCard = new CuratorsWard();
        Permanent ward = new Permanent(wardCard);
        ward.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(auraController.getId()).add(ward);

        return artifact;
    }

    private void setupAndCastDoomBlade(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new DoomBlade()));
        harness.addMana(controller, ManaColor.BLACK, 2);
        harness.castInstant(controller, 0, target.getId());
    }

    private void setupAndCastShatter(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new Shatter()));
        harness.addMana(controller, ManaColor.RED, 2);
        harness.castInstant(controller, 0, target.getId());
    }

    private void setupAndCastDisperse(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new Disperse()));
        harness.addMana(controller, ManaColor.BLUE, 2);
        harness.castInstant(controller, 0, target.getId());
    }
}
