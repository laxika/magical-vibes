package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.g.GuanYuSaintedWarrior;
import com.github.laxika.magicalvibes.cards.z.ZhangFeiFierceWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiuBeiLordOfShu.class, GuanYuSaintedWarrior.class, ZhangFeiFierceWarrior.class,
        AlertShuInfantry.class})
class LiuBeiLordOfShuTest extends BaseCardTest {

    @Test
    @DisplayName("No boost from an irrelevant creature")
    void noBoostFromIrrelevantCreature() {
        Permanent liuBei = harness.addToBattlefieldAndReturn(player1, new LiuBeiLordOfShu());
        int powerWithoutMatchingPermanent = gqs.getEffectivePower(gd, liuBei);
        int toughnessWithoutMatchingPermanent = gqs.getEffectiveToughness(gd, liuBei);

        harness.addToBattlefield(player1, new AlertShuInfantry());

        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(powerWithoutMatchingPermanent);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(toughnessWithoutMatchingPermanent);
    }

    @Test
    @DisplayName("Gets +2/+2 while controlling Guan Yu, Sainted Warrior")
    void boostFromGuanYu() {
        Permanent liuBei = harness.addToBattlefieldAndReturn(player1, new LiuBeiLordOfShu());
        int powerWithoutMatchingPermanent = gqs.getEffectivePower(gd, liuBei);
        int toughnessWithoutMatchingPermanent = gqs.getEffectiveToughness(gd, liuBei);

        harness.addToBattlefield(player1, new GuanYuSaintedWarrior());

        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(powerWithoutMatchingPermanent + 2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(toughnessWithoutMatchingPermanent + 2);
    }

    @Test
    @DisplayName("Gets +2/+2 while controlling a permanent named Zhang Fei, Fierce Warrior")
    void boostFromZhangFei() {
        Permanent liuBei = harness.addToBattlefieldAndReturn(player1, new LiuBeiLordOfShu());
        int powerWithoutMatchingPermanent = gqs.getEffectivePower(gd, liuBei);
        int toughnessWithoutMatchingPermanent = gqs.getEffectiveToughness(gd, liuBei);

        harness.addToBattlefield(player1, new ZhangFeiFierceWarrior());

        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(powerWithoutMatchingPermanent + 2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(toughnessWithoutMatchingPermanent + 2);
    }

    @Test
    @DisplayName("Boost does not stack across both named permanents")
    void boostDoesNotStack() {
        Permanent liuBei = harness.addToBattlefieldAndReturn(player1, new LiuBeiLordOfShu());
        int powerWithoutMatchingPermanent = gqs.getEffectivePower(gd, liuBei);
        int toughnessWithoutMatchingPermanent = gqs.getEffectiveToughness(gd, liuBei);

        harness.addToBattlefield(player1, new GuanYuSaintedWarrior());
        harness.addToBattlefield(player1, new ZhangFeiFierceWarrior());

        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(powerWithoutMatchingPermanent + 2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(toughnessWithoutMatchingPermanent + 2);
    }

    @Test
    @DisplayName("Opponent's Guan Yu does not grant the boost")
    void opponentGuanYuDoesNotCount() {
        Permanent liuBei = harness.addToBattlefieldAndReturn(player1, new LiuBeiLordOfShu());
        int powerWithoutMatchingPermanent = gqs.getEffectivePower(gd, liuBei);
        int toughnessWithoutMatchingPermanent = gqs.getEffectiveToughness(gd, liuBei);

        harness.addToBattlefield(player2, new GuanYuSaintedWarrior());

        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(powerWithoutMatchingPermanent);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(toughnessWithoutMatchingPermanent);
    }

    @Test
    @DisplayName("Horsemanship prevents blocking Liu Bei with a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new AlertShuInfantry());
        Permanent liuBei = addCreatureReady(player1, new LiuBeiLordOfShu());
        liuBei.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(liuBei);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Horsemanship allows blocking Liu Bei with a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new GuanYuSaintedWarrior());
        Permanent liuBei = addCreatureReady(player1, new LiuBeiLordOfShu());
        liuBei.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(liuBei);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Loses the boost when the named permanent leaves the battlefield")
    void losesBoostWhenNamedLeaves() {
        Permanent liuBei = harness.addToBattlefieldAndReturn(player1, new LiuBeiLordOfShu());
        Permanent guanYu = harness.addToBattlefieldAndReturn(player1, new GuanYuSaintedWarrior());

        int boostedPower = gqs.getEffectivePower(gd, liuBei);
        int boostedToughness = gqs.getEffectiveToughness(gd, liuBei);

        gd.playerBattlefields.get(player1.getId()).remove(guanYu);

        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(boostedPower - 2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(boostedToughness - 2);
    }

}
