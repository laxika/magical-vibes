package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EagleOfTheWatch;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.w.WarWingSiren;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyspearCavalry.class, GoldenHind.class, WarWingSiren.class, EagleOfTheWatch.class})
class SkyspearCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SkyspearCavalry());
        addCreatureReady(player2, new GoldenHind());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Double strike deals first-strike and regular combat damage")
    void doubleStrikeDealsDamageInBothCombatDamageSteps() {
        addCreatureReady(player1, new SkyspearCavalry());
        addCreatureReady(player2, new WarWingSiren());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Skyspear Cavalry");
        harness.assertInGraveyard(player2, "War-Wing Siren");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unblocked double strike deals damage twice to the defending player")
    void unblockedDoubleStrikeDealsFourDamage() {
        addCreatureReady(player1, new SkyspearCavalry());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A blocker killed by first strike cannot retaliate or let damage through")
    void firstStrikeKillsFlyingBlockerWithoutDamageSpillingToPlayer() {
        addCreatureReady(player1, new SkyspearCavalry());
        addCreatureReady(player2, new EagleOfTheWatch());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Skyspear Cavalry");
        harness.assertInGraveyard(player2, "Eagle of the Watch");
        harness.assertLife(player2, 20);
    }
}
