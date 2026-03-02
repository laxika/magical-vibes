package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IchorWellspringTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ETB and death draw effects")
    void hasCorrectEffects() {
        IchorWellspring card = new IchorWellspring();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(DrawCardEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("Casting Ichor Wellspring puts it on stack as artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ichor Wellspring");
    }

    @Test
    @DisplayName("Resolving artifact spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell

        harness.assertOnBattlefield(player1, "Ichor Wellspring");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ichor Wellspring");
    }

    @Test
    @DisplayName("ETB trigger draws a card")
    void etbDrawsCard() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("Destroying Ichor Wellspring puts death trigger on stack and draws a card")
    void deathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new IchorWellspring());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Use Shatter to destroy the Wellspring
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var targetId = harness.getPermanentId(player1, "Ichor Wellspring");
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities(); // resolve Shatter — destroys Wellspring

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve death trigger

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ichor Wellspring");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }
}
