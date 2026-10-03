package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathgazeCockatrice.class, CoralMerfolk.class, GiantSpider.class, Fog.class})
class DeathgazeCockatriceTest extends BaseCardTest {

    @Test
    void flyingPreventsBlockingByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new DeathgazeCockatrice());
        addCreatureReady(player2, new CoralMerfolk());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockDeathgazeCockatrice() {
        addCreatureReady(player1, new DeathgazeCockatrice());
        addCreatureReady(player2, new DeathgazeCockatrice());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Deathgaze Cockatrice");
        harness.assertInGraveyard(player2, "Deathgaze Cockatrice");
        harness.assertLife(player2, 20);
    }
    @Test
    void unblockedAttackDealsNormalDamageToPlayer() {
        addCreatureReady(player1, new DeathgazeCockatrice());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void deathtouchDestroysReachBlockerDespiteItsGreaterToughness() {
        addCreatureReady(player1, new DeathgazeCockatrice());
        addCreatureReady(player2, new GiantSpider());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Deathgaze Cockatrice");
        harness.assertLife(player2, 20);
    }

    @Test
    void flyingCreatureCanBlockGroundAttackerAndKillItWithDeathtouch() {
        addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player2, new DeathgazeCockatrice());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Deathgaze Cockatrice");
        harness.assertLife(player2, 20);
    }

    @Test
    void preventedCombatDamageDoesNotDestroyBlockerWithDeathtouch() {
        addCreatureReady(player1, new DeathgazeCockatrice());
        addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.castAndResolveInstant(player1, 0);
        resolveCombat();

        harness.assertOnBattlefield(player1, "Deathgaze Cockatrice");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
    }
}

