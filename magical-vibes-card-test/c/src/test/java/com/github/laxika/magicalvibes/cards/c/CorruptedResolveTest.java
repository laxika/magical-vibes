package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthWellspring;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorruptedResolve.class, GrizzlyBears.class, MycosynthWellspring.class})
class CorruptedResolveTest extends BaseCardTest {

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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not counter a spell when its controller is not poisoned")
    void doesNotCounterWhenControllerNotPoisoned() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Grizzly Bears resolves

        // Spell resolves — creature enters battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
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
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Grizzly Bears resolves

        // Spell resolves — creature enters battlefield because player1 is not poisoned
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player2, "Corrupted Resolve");
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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player2, "Corrupted Resolve");
    }

    @Test
    @DisplayName("Checks poison at resolution when the controller becomes poisoned after casting")
    void countersWhenControllerBecomesPoisonedBeforeResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, bears.getId());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not counter if the controller loses all poison before resolution")
    void doesNotCounterWhenPoisonRemovedBeforeResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, bears.getId());
        gd.playerPoisonCounters.put(player1.getId(), 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Corrupted Resolve");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a noncreature spell without triggering its battlefield abilities")
    void countersArtifactSpell() {
        MycosynthWellspring wellspring = new MycosynthWellspring();
        harness.setHand(player1, List.of(wellspring));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.setHand(player2, List.of(new CorruptedResolve()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.castAndResolveInstant(player2, 0, wellspring.getId());

        harness.assertInGraveyard(player1, "Mycosynth Wellspring");
        harness.assertNotOnBattlefield(player1, "Mycosynth Wellspring");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can counter its caster's own spell when that player is poisoned")
    void countersOwnSpellWhenPoisoned() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new CorruptedResolve()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Corrupted Resolve");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
