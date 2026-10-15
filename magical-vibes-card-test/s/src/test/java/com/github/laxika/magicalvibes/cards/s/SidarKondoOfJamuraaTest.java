package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WoollySpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SidarKondoOfJamuraa.class, GrizzlyBears.class, AirElemental.class,
        WoollySpider.class, HillGiant.class, GiantGrowth.class})
class SidarKondoOfJamuraaTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's ground creature can't block a creature with power 2 or less")
    void opponentGroundCreatureCannotBlockLowPowerCreature() {
        Permanent sidar = addCreatureReady(player1, new SidarKondoOfJamuraa());
        sidar.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(sidar)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying or reach");
    }

    @Test
    @DisplayName("Flying and reach creatures can block a low-power creature")
    void flyingAndReachCreaturesCanBlockLowPowerCreature() {
        Permanent sidar = addCreatureReady(player1, new SidarKondoOfJamuraa());
        sidar.setAttacking(true);
        Permanent flier = addCreatureReady(player2, new AirElemental());
        Permanent reachCreature = addCreatureReady(player2, new WoollySpider());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(flier),
                        gd.playerBattlefields.get(player1.getId()).indexOf(sidar)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(reachCreature),
                        gd.playerBattlefields.get(player1.getId()).indexOf(sidar))));
        assertThat(flier.isBlocking()).isTrue();
        assertThat(reachCreature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A ground creature can block an attacker with power greater than 2")
    void groundCreatureCanBlockHighPowerCreature() {
        addCreatureReady(player1, new SidarKondoOfJamuraa());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction does not affect creatures controlled by Sidar's controller")
    void ownGroundCreatureCanBlockLowPowerOpponentCreature() {
        addCreatureReady(player2, new SidarKondoOfJamuraa());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flanking gives a non-flanking reach blocker -1/-1")
    void flankingReducesNonFlankingBlocker() {
        Permanent sidar = addCreatureReady(player1, new SidarKondoOfJamuraa());
        sidar.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WoollySpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(sidar))));
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(-1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Sidar protects other low-power attackers while not attacking himself")
    void protectsOtherLowPowerAttackers() {
        addCreatureReady(player1, new SidarKondoOfJamuraa());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying or reach");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Increasing power above two makes a creature blockable by ground creatures")
    void restrictionUsesCurrentPowerAfterGiantGrowth() {
        addCreatureReady(player1, new SidarKondoOfJamuraa());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Sidar's restriction ends when he leaves the battlefield")
    void restrictionEndsWhenSidarLeavesBattlefield() {
        Permanent sidar = addCreatureReady(player1, new SidarKondoOfJamuraa());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(sidar);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Each non-flanking blocker receives its own flanking penalty")
    void flankingReducesEachNonFlankingBlocker() {
        Permanent sidar = addCreatureReady(player1, new SidarKondoOfJamuraa());
        sidar.setAttacking(true);
        Permanent first = addCreatureReady(player2, new AirElemental());
        Permanent second = addCreatureReady(player2, new WoollySpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(-1);
        assertThat(first.getToughnessModifier()).isEqualTo(-1);
        assertThat(second.getPowerModifier()).isEqualTo(-1);
        assertThat(second.getToughnessModifier()).isEqualTo(-1);
    }
}
