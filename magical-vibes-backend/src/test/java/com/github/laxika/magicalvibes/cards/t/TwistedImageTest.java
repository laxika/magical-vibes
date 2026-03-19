package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwistedImageTest extends BaseCardTest {

    @Test
    @DisplayName("Twisted Image has correct card properties")
    void hasCorrectProperties() {
        TwistedImage card = new TwistedImage();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(SwitchPowerToughnessEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Casting Twisted Image puts it on the stack with target creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwistedImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Twisted Image");
        assertThat(entry.getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Resolving Twisted Image switches power and toughness and draws a card")
    void switchesPowerToughnessAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player1, List.of(new TwistedImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // Grizzly Bears is 2/2 — after switch it should still be 2/2
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.isPowerToughnessSwitched()).isTrue();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        // Drew a card
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Switching power and toughness on asymmetric creature changes values")
    void switchesAsymmetricCreature() {
        // Use a creature with different P/T by boosting first
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // Grizzly Bears is 2/2, give it +1/+0 to make it 3/2
        bear.setPowerModifier(1);
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);

        harness.setHand(player1, List.of(new TwistedImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // After switch: power should be raw toughness (2+0=2), toughness should be raw power (2+1=3)
        assertThat(bear.isPowerToughnessSwitched()).isTrue();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Switch wears off at cleanup step")
    void switchWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setPowerModifier(1); // Make it 3/2

        harness.setHand(player1, List.of(new TwistedImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // Move to cleanup
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // After cleanup, modifiers and switch are all reset
        assertThat(bear.isPowerToughnessSwitched()).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Twisted Image fizzles and does not draw when target is removed")
    void fizzlesAndDoesNotDrawWhenTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TwistedImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Twisted Image")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new TwistedImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Twisted Image goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwistedImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Twisted Image"));
    }
}
