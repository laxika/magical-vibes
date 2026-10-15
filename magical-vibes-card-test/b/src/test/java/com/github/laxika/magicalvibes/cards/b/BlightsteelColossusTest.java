package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.g.GravePact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightsteelColossus.class, Demolish.class, CruelEdict.class,
        GravePact.class, GrizzlyBears.class, Millstone.class, Cancel.class, MindRot.class, Humble.class,
        RestInPeace.class})
class BlightsteelColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Blightsteel Colossus resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new BlightsteelColossus(), "{12}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blightsteel Colossus");
    }

    @Test
    @DisplayName("Indestructible prevents destruction by Demolish")
    void indestructiblePreventsDestruction() {
        harness.addToBattlefield(player2, new BlightsteelColossus());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Blightsteel Colossus");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // Still on battlefield — indestructible
        harness.assertOnBattlefield(player2, "Blightsteel Colossus");
        // NOT in graveyard
        harness.assertNotInGraveyard(player2, "Blightsteel Colossus");
        // Log confirms indestructible
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Blightsteel Colossus") && log.contains("indestructible"));
    }

    @Test
    @DisplayName("When sacrificed, shuffled into library instead of going to graveyard")
    void replacementEffectOnSacrifice() {
        harness.addToBattlefield(player2, new BlightsteelColossus());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Not on battlefield
        harness.assertNotOnBattlefield(player2, "Blightsteel Colossus");
        // NOT in graveyard — replacement effect
        harness.assertNotInGraveyard(player2, "Blightsteel Colossus");
        // Shuffled into library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Blightsteel Colossus"));
        // Log confirms replacement
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Blightsteel Colossus") && log.contains("shuffled into its owner's library instead"));
    }

    @Test
    @DisplayName("When milled, shuffled into library instead of going to graveyard")
    void replacementEffectOnMill() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Set up player2's library with Blightsteel Colossus on top and a normal card below
        harness.setLibrary(player2, List.of(new BlightsteelColossus(), new GrizzlyBears()));

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

    @Test
    @DisplayName("Sacrifice does not trigger Grave Pact because replacement effect means it never dies")
    void replacementEffectSuppressesDeathTriggers() {
        // Player 1 has Grave Pact (when a creature you control dies, each opponent sacrifices a creature)
        harness.addToBattlefield(player1, new GravePact());
        // Player 1 also has Blightsteel Colossus
        harness.addToBattlefield(player1, new BlightsteelColossus());
        // Player 2 has a creature that should NOT be forced to sacrifice
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player 2 casts Cruel Edict targeting player 1 — forces sacrifice of Blightsteel Colossus
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        // Blightsteel Colossus was shuffled into library, not put into graveyard
        harness.assertNotOnBattlefield(player1, "Blightsteel Colossus");
        harness.assertNotInGraveyard(player1, "Blightsteel Colossus");
        // Grave Pact should NOT have triggered — opponent's creature is still alive
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discarded Colossus is revealed and shuffled into its owner's library")
    void replacementEffectOnDiscard() {
        BlightsteelColossus colossus = new BlightsteelColossus();
        harness.setHand(player2, List.of(colossus));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Blightsteel Colossus");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(colossus);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Blightsteel Colossus") && log.contains("revealed"));
    }

    @Test
    @DisplayName("Countered Colossus is shuffled into its owner's library")
    void replacementEffectOnCounter() {
        BlightsteelColossus colossus = new BlightsteelColossus();
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, colossus, "{12}");
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, colossus.getId());

        harness.assertNotOnBattlefield(player1, "Blightsteel Colossus");
        harness.assertNotInGraveyard(player1, "Blightsteel Colossus");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(colossus);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblocked Colossus deals eleven poison counters without reducing life")
    void unblockedCombatDealsPoison() {
        addCreatureReady(player1, new BlightsteelColossus());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(11);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Trample assigns lethal infect damage to a blocker and excess as poison")
    void trampleAndInfectAgainstBlocker() {
        addCreatureReady(player1, new BlightsteelColossus());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 9));

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Blightsteel Colossus");
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Owner chooses between shuffling and exiling a milled Colossus")
    void competingGraveyardReplacementsRequireChoice() {
        harness.addToBattlefield(player1, new RestInPeace());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, List.of(new BlightsteelColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertNotInGraveyard(player2, "Blightsteel Colossus");
    }

    @Test
    @DisplayName("Zero toughness bypasses indestructible but still applies the shuffle replacement")
    void zeroToughnessStillShufflesIntoLibrary() {
        BlightsteelColossus colossus = new BlightsteelColossus();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, colossus);
        harness.setLibrary(player1, List.of());
        permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 11);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Blightsteel Colossus");
        harness.assertNotInGraveyard(player1, "Blightsteel Colossus");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(colossus);
    }

    @Test
    @DisplayName("Colossus without abilities enters the graveyard when sacrificed")
    void noShuffleReplacementAfterLosingAbilities() {
        BlightsteelColossus colossus = new BlightsteelColossus();
        harness.addToBattlefield(player2, colossus);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Humble(), new CruelEdict()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Blightsteel Colossus"));
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Blightsteel Colossus");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(colossus);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
