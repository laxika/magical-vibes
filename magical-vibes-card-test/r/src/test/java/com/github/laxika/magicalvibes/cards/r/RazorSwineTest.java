package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlindingSouleater;
import com.github.laxika.magicalvibes.cards.i.ImmolatingSouleater;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorSwine.class, BlindingSouleater.class, ImmolatingSouleater.class,
        PorcelainLegionnaire.class})
class RazorSwineTest extends BaseCardTest {

    @Test
    void unblockedAttackDealsTwoPoisonCountersOnlyOnceWithoutLifeLoss() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RazorSwine());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void killsBlockerBeforeItCanDealRegularDamage() {
        Permanent swine = addCreatureReady(player1, new RazorSwine());
        addCreatureReady(player2, new ImmolatingSouleater());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Razor Swine");
        assertThat(swine.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Immolating Souleater");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void infectWeakensSurvivingBlockerBeforeRegularDamage() {
        Permanent swine = addCreatureReady(player1, new RazorSwine());
        Permanent blocker = addCreatureReady(player2, new BlindingSouleater());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Razor Swine");
        harness.assertOnBattlefield(player2, "Blinding Souleater");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(swine.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockingSwineKillsAttackerBeforeRegularDamage() {
        addCreatureReady(player1, new ImmolatingSouleater());
        Permanent swine = addCreatureReady(player2, new RazorSwine());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Immolating Souleater");
        harness.assertOnBattlefield(player2, "Razor Swine");
        assertThat(swine.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void opposingFirstStrikerDealsDamageSimultaneouslyWithInfect() {
        addCreatureReady(player1, new RazorSwine());
        addCreatureReady(player2, new PorcelainLegionnaire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Razor Swine");
        harness.assertInGraveyard(player2, "Porcelain Legionnaire");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
