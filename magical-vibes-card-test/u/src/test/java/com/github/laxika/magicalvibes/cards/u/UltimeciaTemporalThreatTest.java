package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltimeciaTemporalThreat.class, GrizzlyBears.class, Forest.class})
class UltimeciaTemporalThreatTest extends BaseCardTest {

    @Test
    @DisplayName("Entering taps all creatures opponents control but not creatures you control")
    void entersAndTapsOpponentsCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new UltimeciaTemporalThreat()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws once for each creature you control that deals combat damage to a player")
    void drawsForEachCreatureDealingCombatDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new UltimeciaTemporalThreat());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when your creature deals no combat damage to a player")
    void doesNotDrawWhenBlocked() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new UltimeciaTemporalThreat());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ultimecia's own combat damage draws one card regardless of damage amount")
    void drawsForItsOwnCombatDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new UltimeciaTemporalThreat());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opposing creature's combat damage does not draw for your Ultimecia")
    void opposingCombatDamageDrawsOnlyForOpposingController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        Permanent ownUltimecia = addCreatureReady(player1, new UltimeciaTemporalThreat());
        ownUltimecia.tap();
        addCreatureReady(player2, new UltimeciaTemporalThreat());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The entry ability leaves opposing noncreature permanents untapped")
    void entersWithNoOpposingCreatures() {
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UltimeciaTemporalThreat()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(opposingLand.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Ultimecia, Temporal Threat").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
