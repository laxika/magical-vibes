package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GnarlidColony;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VineGecko.class, GnarlidColony.class})
class VineGeckoTest extends BaseCardTest {

    @Test
    void firstKickedSpellEachTurnCostsOneLessAndPutsCounterOnVineGecko() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonKickedSpellDoesNotUseOrConsumeTheReduction() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlyTheFirstKickedSpellGetsTheReduction() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new GnarlidColony(), new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingAnUnkickedSpellLeavesTheFirstKickedSpellReductionAvailable() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new GnarlidColony(), new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Gnarlid Colony")).isEqualTo(2);
    }

    @Test
    void laterKickedSpellsStillPutCountersOnVineGecko() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new GnarlidColony(), new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Gnarlid Colony")).isEqualTo(2);
    }

    @Test
    void multipleGeckosReduceTheSameFirstSpellAndEachGetsACounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VineGecko());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VineGecko());
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Gnarlid Colony");
    }

    @Test
    void kickedSpellCastBeforeGeckoEnteredAlreadyConsumesTheReduction() {
        harness.setHand(player1, List.of(new GnarlidColony(), new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new VineGecko());

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsKickedSpellDoesNotGetDiscountOrTriggerGecko() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GnarlidColony()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castKickedCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castKickedCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Gnarlid Colony");
    }
    @Test
    void reductionIsAvailableAgainOnTheNextTurn() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new GnarlidColony(), new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vine Gecko").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Gnarlid Colony")).isEqualTo(2);
    }

}
