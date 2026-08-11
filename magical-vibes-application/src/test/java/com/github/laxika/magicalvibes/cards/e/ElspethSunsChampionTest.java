package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElspethSunsChampionTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates three Soldier tokens")
    void plusOneCreatesThreeSoldiers() {
        Permanent elspeth = addReadyElspeth(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(3);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-3 destroys creatures with power 4 or greater and leaves smaller creatures")
    void minusThreeDestroysCreaturesWithPowerAtLeastFour() {
        Permanent elspeth = addReadyElspeth(4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-7 gives an emblem that boosts and grants flying to controlled creatures")
    void minusSevenCreatesCreatureAnthemEmblem() {
        Permanent elspeth = addReadyElspeth(7);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.FLYING)).isFalse();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyElspeth(int loyalty) {
        Permanent perm = new Permanent(new ElspethSunsChampion());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
