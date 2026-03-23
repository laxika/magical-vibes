package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfControllerPoisonedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorruptedResolveTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct card properties")
    void hasCorrectProperties() {
        CorruptedResolve card = new CorruptedResolve();

        assertThat(EffectResolution.needsSpellTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isNull();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CounterSpellIfControllerPoisonedEffect.class);
    }

    // ===== Counters when controller is poisoned =====

    @Test
    @DisplayName("Counters a spell when its controller is poisoned")
    void countersSpellWhenControllerPoisoned() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Counters a spell when its controller has multiple poison counters")
    void countersSpellWhenControllerHasMultiplePoisonCounters() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.playerPoisonCounters.put(player1.getId(), 7);

        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Does not counter when controller is not poisoned =====

    @Test
    @DisplayName("Does not counter a spell when its controller is not poisoned")
    void doesNotCounterWhenControllerNotPoisoned() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Corrupted Resolve resolves — does not counter
        harness.passBothPriorities(); // Grizzly Bears resolves

        // Spell resolves — creature enters battlefield
        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not counter when only the caster of Corrupted Resolve is poisoned")
    void doesNotCounterWhenOnlyCasterPoisoned() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Corrupted Resolve resolves — does not counter
        harness.passBothPriorities(); // Grizzly Bears resolves

        // Spell resolves — creature enters battlefield because player1 is not poisoned
        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Corrupted Resolve goes to graveyard =====

    @Test
    @DisplayName("Goes to caster's graveyard after resolving (counter succeeds)")
    void goesToGraveyardAfterCountering() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Corrupted Resolve"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving (counter fails)")
    void goesToGraveyardWhenCounterFails() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Corrupted Resolve"));
    }
}
