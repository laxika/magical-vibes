package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GaeasHerald;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneToAsh.class, GrizzlyBears.class, Millstone.class, GaeasHerald.class})
class BoneToAshTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a creature spell")
    void castingPutsOnStackTargetingCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry boneToAshEntry = gd.stack.getLast();
        assertThat(boneToAshEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(boneToAshEntry.getCard()).isInstanceOf(BoneToAsh.class);
        assertThat(boneToAshEntry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving counters the creature spell")
    void countersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        // Countered creature goes to owner's graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Does not enter the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving draws a card for the caster")
    void drawsACard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        // Bone to Ash caster drew a card (hand was emptied by casting, then drew 1)
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Bone to Ash goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Bone to Ash");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        // Remove target from stack before Bone to Ash resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Bone to Ash still goes to graveyard
        harness.assertInGraveyard(player2, "Bone to Ash");
    }

    @Test
    @DisplayName("Does not draw when its only target has left the stack")
    void doesNotDrawWhenTargetLeavesStack() {
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears draw = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.setLibrary(player2, List.of(draw));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(draw);
        harness.assertInGraveyard(player2, "Bone to Ash");
    }

    @Test
    @DisplayName("Draws even when the targeted creature spell cannot be countered")
    void drawsWhenCreatureCannotBeCountered() {
        harness.addToBattlefield(player1, new GaeasHerald());
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears draw = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.setLibrary(player2, List.of(draw));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(draw);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(bears);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Bone to Ash");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
