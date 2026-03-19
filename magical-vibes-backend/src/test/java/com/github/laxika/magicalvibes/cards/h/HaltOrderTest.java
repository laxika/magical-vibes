package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HaltOrderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Halt Order has correct card properties")
    void hasCorrectProperties() {
        HaltOrder card = new HaltOrder();

        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.ARTIFACT_SPELL)),
                "Target must be an artifact spell."
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CounterSpellEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting an artifact spell")
    void castingPutsOnStackTargetingArtifactSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HaltOrder()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry haltOrderEntry = gd.stack.getLast();
        assertThat(haltOrderEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(haltOrderEntry.getCard().getName()).isEqualTo("Halt Order");
        assertThat(haltOrderEntry.getTargetId()).isEqualTo(millstone.getId());
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new HaltOrder()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters the artifact spell")
    void countersArtifactSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HaltOrder()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Countered artifact goes to owner's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Millstone"));
        // Does not enter the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Millstone"));
    }

    @Test
    @DisplayName("Resolving draws a card for the caster")
    void drawsACard() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HaltOrder()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());
        harness.passBothPriorities();

        // Halt Order caster drew a card (hand was emptied by casting, then drew 1)
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Halt Order goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HaltOrder()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Halt Order"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HaltOrder()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());

        // Remove target from stack before Halt Order resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Millstone"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Halt Order still goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Halt Order"));
    }
}
