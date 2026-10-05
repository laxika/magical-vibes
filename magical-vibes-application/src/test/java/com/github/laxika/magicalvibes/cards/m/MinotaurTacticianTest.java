package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.c.CoalitionFlag;
import com.github.laxika.magicalvibes.cards.j.JungleBarrier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinotaurTactician.class, AirElemental.class, BenalishKnight.class, CoalitionFlag.class, JungleBarrier.class})
class MinotaurTacticianTest extends BaseCardTest {

    @Test
    void getsOneBoostForAWhiteCreature() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player1, new BenalishKnight());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 1);
    }

    @Test
    void getsOneBoostForABlueCreature() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 1);
    }

    @Test
    void getsBothBoostsWhenWhiteAndBlueCreaturesAreControlled() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 2);
    }

    @Test
    void opponentCreatureAndWhiteNoncreatureDoNotProvideBoosts() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addToBattlefield(player2, new AirElemental());
        Permanent flag = harness.addToBattlefieldAndReturn(player1, new CoalitionFlag());
        flag.setAttachedTo(tactician.getId());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness);
    }

    @Test
    void multipleCreaturesOfEachColorDoNotMultiplyTheBoosts() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);
        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 2);
    }

    @Test
    void losingTheWhiteCreatureRemovesOnlyTheWhiteBoost() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 2);

        gd.playerBattlefields.get(player1.getId()).remove(knight);
        gd.playerGraveyards.get(player1.getId()).add(knight.getCard());
        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 1);

        gd.playerBattlefields.get(player1.getId()).remove(elemental);
        gd.playerGraveyards.get(player1.getId()).add(elemental.getCard());
        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness);
    }

    @Test
    void multicoloredBlueCreatureProvidesTheBlueBoost() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);
        harness.addToBattlefield(player1, new JungleBarrier());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 1);
    }

    @Test
    void hasteAllowsAttackingWhileSummoningSickWithoutEitherBoost() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new MinotaurTactician());
        tactician.setSummoningSick(true);

        assertThat(als.canAttack(gd, tactician, player1.getId())).isTrue();
    }
}
