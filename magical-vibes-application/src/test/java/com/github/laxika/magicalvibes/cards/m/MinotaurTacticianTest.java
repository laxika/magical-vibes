package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinotaurTactician.class, AirElemental.class, BenalishKnight.class, Plains.class})
class MinotaurTacticianTest extends BaseCardTest {

    @Test
    void getsOneBoostForAWhiteCreature() {
        harness.addToBattlefield(player1, new MinotaurTactician());
        Permanent tactician = findPermanent(player1, "Minotaur Tactician");
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player1, new BenalishKnight());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 1);
    }

    @Test
    void getsOneBoostForABlueCreature() {
        harness.addToBattlefield(player1, new MinotaurTactician());
        Permanent tactician = findPermanent(player1, "Minotaur Tactician");
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 1);
    }

    @Test
    void getsBothBoostsWhenWhiteAndBlueCreaturesAreControlled() {
        harness.addToBattlefield(player1, new MinotaurTactician());
        Permanent tactician = findPermanent(player1, "Minotaur Tactician");
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness + 2);
    }

    @Test
    void opponentCreatureAndWhiteNoncreatureDoNotProvideBoosts() {
        harness.addToBattlefield(player1, new MinotaurTactician());
        Permanent tactician = findPermanent(player1, "Minotaur Tactician");
        int basePower = gqs.getEffectivePower(gd, tactician);
        int baseToughness = gqs.getEffectiveToughness(gd, tactician);

        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(baseToughness);
    }
}
