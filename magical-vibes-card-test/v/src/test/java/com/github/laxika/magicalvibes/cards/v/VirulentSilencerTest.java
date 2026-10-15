package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MechanAssembler;
import com.github.laxika.magicalvibes.cards.f.FungalColossus;
import com.github.laxika.magicalvibes.cards.z.ZookeeperMechan;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirulentSilencer.class, MechanAssembler.class, FungalColossus.class, ZookeeperMechan.class})
class VirulentSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken artifact creature dealing combat damage gives two poison counters")
    void nontokenArtifactCreatureGivesTwoPoisonCounters() {
        addCreatureReady(player1, new VirulentSilencer()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-artifact creature does not trigger Virulent Silencer")
    void nonArtifactCreatureDoesNotTrigger() {
        addCreatureReady(player1, new VirulentSilencer());
        addCreatureReady(player1, new FungalColossus()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A token artifact creature does not trigger Virulent Silencer")
    void tokenArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new MechanAssembler());
        harness.enterBattlefieldAndReturn(player1, new VirulentSilencer());
        resolveAllTriggers();

        Permanent robot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        robot.setSummoningSick(false);
        robot.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Each matching damage dealer gives two poison counters")
    void eachArtifactCreatureTriggersSeparately() {
        addCreatureReady(player1, new VirulentSilencer()).setAttacking(true);
        addCreatureReady(player1, new ZookeeperMechan()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Each Silencer triggers for another artifact creature")
    void multipleSilencersTriggerIndependently() {
        addCreatureReady(player1, new VirulentSilencer());
        addCreatureReady(player1, new VirulentSilencer());
        addCreatureReady(player1, new ZookeeperMechan()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's artifact creature does not trigger Silencer")
    void opponentsArtifactCreatureDoesNotTrigger() {
        addCreatureReady(player1, new VirulentSilencer());
        addCreatureReady(player2, new ZookeeperMechan()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
