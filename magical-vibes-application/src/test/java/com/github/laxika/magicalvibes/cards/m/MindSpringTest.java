package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindSpringTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Mind Spring has correct effects")
    void hasCorrectEffects() {
        MindSpring card = new MindSpring();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DrawXCardsEffect.class);
    }

    @Test
    @DisplayName("Mind Spring does not require a target")
    void doesNotRequireTarget() {
        assertThat(EffectResolution.needsTarget(new MindSpring())).isFalse();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3: {3}{U}{U} = 5

        harness.castSorcery(player1, 0, 3);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Mind Spring");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    // ===== Resolution: draw cards =====

    @Test
    @DisplayName("X=3 draws 3 cards for the controller")
    void xEqualsThreeDrawsThreeCards() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3: {3}{U}{U} = 5
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("X=0 draws no cards")
    void xZeroDrawsNoCards() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 2); // X=0: {0}{U}{U} = 2
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("X=1 draws 1 card")
    void xOneDrawsOneCard() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=1: {1}{U}{U} = 3
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    // ===== Graveyard and stack cleanup =====

    @Test
    @DisplayName("Mind Spring goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Spring"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
