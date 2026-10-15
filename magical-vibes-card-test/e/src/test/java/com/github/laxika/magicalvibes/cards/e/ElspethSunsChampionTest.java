package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.n.NessianAsp;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElspethSunsChampion.class, TravelingPhilosopher.class, NessianAsp.class})
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
        harness.addToBattlefield(player1, new TravelingPhilosopher());
        harness.addToBattlefield(player2, new TravelingPhilosopher());
        harness.addToBattlefield(player2, new NessianAsp());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Traveling Philosopher");
        harness.assertOnBattlefield(player2, "Traveling Philosopher");
        harness.assertNotOnBattlefield(player2, "Nessian Asp");
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-7 gives an emblem that boosts and grants flying to controlled creatures")
    void minusSevenCreatesCreatureAnthemEmblem() {
        Permanent elspeth = addReadyElspeth(7);
        harness.addToBattlefield(player1, new TravelingPhilosopher());
        harness.addToBattlefield(player2, new TravelingPhilosopher());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent ownBears = findPermanent(player1, "Traveling Philosopher");
        Permanent opposingBears = findPermanent(player2, "Traveling Philosopher");
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.FLYING)).isFalse();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("+1 makes white 1/1 Soldier creatures only for its controller")
    void soldiersHaveCorrectCharacteristics() {
        addReadyElspeth(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        var soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(soldiers).hasSize(3).allSatisfy(soldier -> {
            assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("-3 uses current power on both sides, including changes before resolution")
    void minusThreeChecksCurrentPowerAtResolution() {
        addReadyElspeth(4);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent smallerCreature = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        smallerCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(smallerCreature)
                .doesNotContain(opposingCreature);
        harness.assertInGraveyard(player1, "Traveling Philosopher");
        harness.assertInGraveyard(player2, "Traveling Philosopher");
    }

    @Test
    @DisplayName("Emblem persists without Elspeth and affects later creatures")
    void emblemBoostsLaterCreaturesAfterElspethLeaves() {
        addReadyElspeth(7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elspeth, Sun's Champion");
        harness.assertInGraveyard(player1, "Elspeth, Sun's Champion");
        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new TravelingPhilosopher());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyElspeth(int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ElspethSunsChampion());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
