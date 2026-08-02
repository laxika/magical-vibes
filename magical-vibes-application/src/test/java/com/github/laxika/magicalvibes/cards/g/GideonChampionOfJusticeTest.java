package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GideonChampionOfJusticeTest extends BaseCardTest {

    @Test
    void plusOneCountsTargetOpponentsCreaturesOnResolution() {
        Permanent gideon = addReadyGideon(player1, 4);
        addReadyCreature(player2);
        addReadyCreature(player2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        addReadyCreature(player2);
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
    }

    @Test
    void zeroSnapshotsLoyaltyForPowerAndToughnessAndPreventsDamage() {
        Permanent gideon = addReadyGideon(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();

        gideon.setCounterCount(CounterType.LOYALTY, 7);
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void minusFifteenExilesAllOtherPermanents() {
        Permanent gideon = addReadyGideon(player1, 16);
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opposingCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(gideon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
    }

    private Permanent addReadyGideon(Player player, int loyalty) {
        Permanent gideon = new Permanent(new GideonChampionOfJustice());
        gideon.setCounterCount(CounterType.LOYALTY, loyalty);
        gideon.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(gideon);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gideon;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
