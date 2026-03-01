package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.g.GravePact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlightsteelColossusTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Blightsteel Colossus has shuffle-into-library replacement effect")
    void hasShuffleIntoLibraryReplacementEffect() {
        BlightsteelColossus card = new BlightsteelColossus();

        assertThat(card.isShufflesIntoLibraryFromGraveyard()).isTrue();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Blightsteel Colossus resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new BlightsteelColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blightsteel Colossus");
    }

    // ===== Indestructible: cannot be destroyed =====

    @Test
    @DisplayName("Indestructible prevents destruction by Demolish")
    void indestructiblePreventsDestruction() {
        harness.addToBattlefield(player2, new BlightsteelColossus());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Blightsteel Colossus");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Still on battlefield — indestructible
        harness.assertOnBattlefield(player2, "Blightsteel Colossus");
        // NOT in graveyard
        harness.assertNotInGraveyard(player2, "Blightsteel Colossus");
        // Log confirms indestructible
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Blightsteel Colossus") && log.contains("indestructible"));
    }

    // ===== Replacement effect: sacrificed =====

    @Test
    @DisplayName("When sacrificed, shuffled into library instead of going to graveyard")
    void replacementEffectOnSacrifice() {
        harness.addToBattlefield(player2, new BlightsteelColossus());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Not on battlefield
        harness.assertNotOnBattlefield(player2, "Blightsteel Colossus");
        // NOT in graveyard — replacement effect
        harness.assertNotInGraveyard(player2, "Blightsteel Colossus");
        // Shuffled into library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Blightsteel Colossus"));
        // Log confirms replacement
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Blightsteel Colossus") && log.contains("shuffled into its owner's library instead"));
    }

    // ===== Replacement effect: milled =====

    @Test
    @DisplayName("When milled, shuffled into library instead of going to graveyard")
    void replacementEffectOnMill() {
        Permanent millstone = addReadyMillstone(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Set up player2's library with Blightsteel Colossus on top and a normal card below
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new BlightsteelColossus());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Blightsteel Colossus should NOT be in graveyard
        harness.assertNotInGraveyard(player2, "Blightsteel Colossus");
        // Blightsteel Colossus should be back in library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Blightsteel Colossus"));
        // Grizzly Bears (normal card) should be in graveyard
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Replacement effect suppresses death triggers (CR 614.6) =====

    @Test
    @DisplayName("Sacrifice does not trigger Grave Pact because replacement effect means it never dies")
    void replacementEffectSuppressesDeathTriggers() {
        // Player 1 has Grave Pact (when a creature you control dies, each opponent sacrifices a creature)
        harness.addToBattlefield(player1, new GravePact());
        // Player 1 also has Blightsteel Colossus
        harness.addToBattlefield(player1, new BlightsteelColossus());
        // Player 2 has a creature that should NOT be forced to sacrifice
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        // Player 2 casts Cruel Edict targeting player 1 — forces sacrifice of Blightsteel Colossus
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Blightsteel Colossus was shuffled into library, not put into graveyard
        harness.assertNotOnBattlefield(player1, "Blightsteel Colossus");
        harness.assertNotInGraveyard(player1, "Blightsteel Colossus");
        // Grave Pact should NOT have triggered — opponent's creature is still alive
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Helpers =====

    private Permanent addReadyMillstone(Player player) {
        Millstone card = new Millstone();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
