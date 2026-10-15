package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaltOrder.class, Millstone.class, GrizzlyBears.class, Memnite.class})
class HaltOrderTest extends BaseCardTest {


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
        assertThat(haltOrderEntry.getCard()).isInstanceOf(HaltOrder.class);
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
        harness.castAndResolveInstant(player2, 0, millstone.getId());

        // Countered artifact goes to owner's graveyard
        harness.assertInGraveyard(player1, "Millstone");
        // Does not enter the battlefield
        harness.assertNotOnBattlefield(player1, "Millstone");
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
        harness.castAndResolveInstant(player2, 0, millstone.getId());

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
        harness.castAndResolveInstant(player2, 0, millstone.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Halt Order");
        assertThat(gd.stack).isEmpty();
    }


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

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Halt Order still goes to graveyard
        harness.assertInGraveyard(player2, "Halt Order");
    }

    @Test
    @DisplayName("Counters an artifact creature spell and draws a card")
    void countersArtifactCreatureAndDraws() {
        Memnite memnite = new Memnite();
        Memnite drawnCard = new Memnite();
        harness.setHand(player1, List.of(memnite));
        harness.setHand(player2, List.of(new HaltOrder()));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, memnite.getId());

        harness.assertInGraveyard(player1, "Memnite");
        harness.assertNotOnBattlefield(player1, "Memnite");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player2, "Halt Order");
    }

    @Test
    @DisplayName("Does not draw when its only target leaves the stack")
    void doesNotDrawWithMissingTarget() {
        Millstone millstone = new Millstone();
        Memnite drawnCard = new Memnite();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new HaltOrder()));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(millstone.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player2, "Halt Order");
    }
}
