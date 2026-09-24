package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GravePact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelColossus.class, CruelEdict.class, Demolish.class, GravePact.class,
        GrizzlyBears.class, Millstone.class, MindRot.class})
class DarksteelColossusTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Darksteel Colossus resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new DarksteelColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Colossus");
    }

    // ===== Indestructible: cannot be destroyed =====

    @Test
    @DisplayName("Indestructible prevents destruction by Demolish")
    void indestructiblePreventsDestruction() {
        harness.addToBattlefield(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Colossus");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Still on battlefield — indestructible
        harness.assertOnBattlefield(player2, "Darksteel Colossus");
        // NOT in graveyard
        harness.assertNotInGraveyard(player2, "Darksteel Colossus");
        // Log confirms indestructible
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Darksteel Colossus") && log.contains("indestructible"));
    }

    // ===== Replacement effect: sacrificed =====

    @Test
    @DisplayName("When sacrificed, shuffled into library instead of going to graveyard")
    void replacementEffectOnSacrifice() {
        harness.addToBattlefield(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Not on battlefield
        harness.assertNotOnBattlefield(player2, "Darksteel Colossus");
        // NOT in graveyard — replacement effect
        harness.assertNotInGraveyard(player2, "Darksteel Colossus");
        // Shuffled into library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Darksteel Colossus"));
        // Log confirms replacement
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Darksteel Colossus") && log.contains("shuffled into its owner's library instead"));
    }

    // ===== Replacement effect: milled =====

    @Test
    @DisplayName("When milled, shuffled into library instead of going to graveyard")
    void replacementEffectOnMill() {
        Permanent millstone = addReadyMillstone(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Set up player2's library with Darksteel Colossus on top and a normal card below
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new DarksteelColossus());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Darksteel Colossus should NOT be in graveyard
        harness.assertNotInGraveyard(player2, "Darksteel Colossus");
        // Darksteel Colossus should be back in library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Darksteel Colossus"));
        // Grizzly Bears (normal card) should be in graveyard
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Replacement effect: discarded =====

    @Test
    @DisplayName("When discarded, shuffled into its owner's library instead of going to the graveyard")
    void replacementEffectOnDiscard() {
        DarksteelColossus colossus = new DarksteelColossus();
        harness.setHand(player2, List.of(colossus));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        harness.assertNotInGraveyard(player2, "Darksteel Colossus");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .contains(colossus);
    }

    // ===== Replacement effect: owner routing =====

    @Test
    @DisplayName("When controlled by another player, shuffled into its owner's library")
    void replacementEffectUsesOwnersLibrary() {
        DarksteelColossus colossus = new DarksteelColossus();
        colossus.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, colossus);

        int ownerDeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int controllerDeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Darksteel Colossus");
        harness.assertNotInGraveyard(player2, "Darksteel Colossus");
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(ownerDeckSizeBefore + 1)
                .contains(colossus);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerDeckSizeBefore);
    }

    // ===== Trample =====

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        addCreatureReady(player1, new DarksteelColossus());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 9));

        harness.assertOnBattlefield(player1, "Darksteel Colossus");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 11);
    }

    // ===== Replacement effect suppresses death triggers (CR 614.6) =====

    @Test
    @DisplayName("Sacrifice does not trigger Grave Pact because replacement effect means it never dies")
    void replacementEffectSuppressesDeathTriggers() {
        // Player 1 has Grave Pact (when a creature you control dies, each opponent sacrifices a creature)
        harness.addToBattlefield(player1, new GravePact());
        // Player 1 also has Darksteel Colossus
        harness.addToBattlefield(player1, new DarksteelColossus());
        // Player 2 has a creature that should NOT be forced to sacrifice
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player 2 casts Cruel Edict targeting player 1 — forces sacrifice of Darksteel Colossus
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Darksteel Colossus was shuffled into library, not put into graveyard
        harness.assertNotOnBattlefield(player1, "Darksteel Colossus");
        harness.assertNotInGraveyard(player1, "Darksteel Colossus");
        // Grave Pact should NOT have triggered — opponent's creature is still alive
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Helpers =====

    private Permanent addReadyMillstone(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Millstone());
        perm.setSummoningSick(false);
        return perm;
    }
}
