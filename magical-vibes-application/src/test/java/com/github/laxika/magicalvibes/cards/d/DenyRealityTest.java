package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DenyRealityTest extends BaseCardTest {

    // ===== Bounce (Return target permanent to its owner's hand) =====

    @Test
    @DisplayName("Resolving returns target creature to its owner's hand")
    void resolvingReturnsCreatureToHand() {
        setupCasterTurn();
        emptyCasterLibrary(); // cascade digs nothing so we isolate the bounce
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castDenyReality(targetId);
        harness.passBothPriorities(); // resolve the cascade trigger (no-op, empty library)
        harness.passBothPriorities(); // resolve the bounce

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can bounce a land (any permanent is a legal target)")
    void bouncesLand() {
        setupCasterTurn();
        emptyCasterLibrary();
        harness.addToBattlefield(player2, new Island());

        UUID targetId = harness.getPermanentId(player2, "Island");
        castDenyReality(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        setupCasterTurn();
        emptyCasterLibrary();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castDenyReality(targetId);
        harness.passBothPriorities(); // resolve cascade trigger first

        gd.playerBattlefields.get(player2.getId()).clear(); // remove the bounce target
        harness.passBothPriorities(); // bounce tries to resolve

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deny Reality"));
    }

    // ===== Cascade =====

    @Test
    @DisplayName("Cascade digs past lands and higher-cost nonlands to the first lesser-cost nonland")
    void cascadeOffersFirstLesserNonland() {
        setupCasterTurn();
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Deny Reality is {3}{U}{B} = mana value 5. Dig should skip the land, stop at Llanowar Elves
        // (MV 1 < 5), and never touch the Grizzly Bears beneath it.
        GrizzlyBears belowHit = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Mountain(), new LlanowarElves(), belowHit));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castDenyReality(targetId);
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("Casting the cascade hit puts it on the stack for free")
    void castingCascadeHitPutsItOnStack() {
        setupCasterTurn();
        harness.addToBattlefield(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Mountain(), new LlanowarElves()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castDenyReality(targetId);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Llanowar Elves")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    // ===== Helpers =====

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void emptyCasterLibrary() {
        gd.playerDecks.get(player1.getId()).clear();
    }

    private void castDenyReality(UUID targetId) {
        harness.setHand(player1, List.of(new DenyReality()));
        harness.addMana(player1, ManaColor.BLUE, 4); // covers {U} + {3} generic
        harness.addMana(player1, ManaColor.BLACK, 1); // covers {B}
        harness.castSorcery(player1, 0, targetId);
    }
}
