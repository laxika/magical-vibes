package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WanShiTongAllKnowing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoscoJustABear.class, WanShiTongAllKnowing.class})
class BoscoJustABearTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Food for each legendary creature you control")
    void createsFoodForEachLegendaryCreature() {
        harness.addToBattlefield(player1, new WanShiTongAllKnowing());

        castBosco();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a Food puts two counters on Bosco and grants trample")
    void sacrificesFoodForCountersAndTrample() {
        Permanent bosco = castBosco();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(bosco), null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(bosco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bosco, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent bosco = castBosco();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(bosco), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bosco, Keyword.TRAMPLE)).isFalse();
        assertThat(bosco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opposing legendary creatures do not increase the Food count")
    void ignoresOpposingLegendaryCreatures() {
        harness.addToBattlefield(player2, new BoscoJustABear());

        castBosco();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    @DisplayName("The legendary creature count is evaluated when the trigger resolves")
    void countsLegendaryCreaturesAtResolution() {
        harness.castFromHand(player1, new BoscoJustABear(), "{4}{G}");
        harness.passBothPriorities();
        Permanent bosco = findPermanent(player1, "Bosco, Just a Bear");
        gd.playerBattlefields.get(player1.getId()).remove(bosco);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Food can be sacrificed immediately to gain three life")
    void foodGainsLife() {
        castBosco();
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(food), null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("A tapped Bosco can sacrifice a tapped Food, with counters added on resolution")
    void tappedPermanentsCanPayFoodCost() {
        Permanent bosco = castBosco();
        bosco.tap();
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(bosco), null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(bosco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, bosco, Keyword.TRAMPLE)).isFalse();
        harness.passBothPriorities();
        assertThat(bosco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bosco, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bosco cannot activate without a Food to sacrifice")
    void cannotActivateWithoutFood() {
        Permanent bosco = harness.addToBattlefieldAndReturn(player1, new BoscoJustABear());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bosco), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bosco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castBosco() {
        harness.castFromHand(player1, new BoscoJustABear(), "{4}{G}");
        resolveAllTriggers();
        return findPermanent(player1, "Bosco, Just a Bear");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
