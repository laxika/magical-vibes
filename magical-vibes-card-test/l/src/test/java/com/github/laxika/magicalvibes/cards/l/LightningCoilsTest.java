package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.o.OmegaMyr;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Lightning Coils")
@CardUsed({LightningCoils.class, OmegaMyr.class, RaiseTheAlarm.class, BarterInBlood.class})
class LightningCoilsTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature you control dying adds a charge counter")
    void nontokenCreatureDeathAddsChargeCounter() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent myr = addCreatureReady(player1, new OmegaMyr());

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent coils = findPermanent(player1, "Lightning Coils");
        assertThat(coils.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(myr.getCard());
    }

    @Test
    @DisplayName("A token creature dying does not add a charge counter")
    void tokenCreatureDeathDoesNotAddChargeCounter() {
        harness.addToBattlefield(player1, new LightningCoils());
        harness.castFromHand(player1, new RaiseTheAlarm(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent coils = findPermanent(player1, "Lightning Coils");
        assertThat(coils.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    @DisplayName("An opponent's nontoken creature dying does not add a charge counter")
    void opponentNontokenCreatureDeathDoesNotAddChargeCounter() {
        harness.addToBattlefield(player1, new LightningCoils());
        addCreatureReady(player2, new OmegaMyr());

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent coils = findPermanent(player1, "Lightning Coils");
        assertThat(coils.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Five charge counters create that many hasty 3/1 Elementals at upkeep")
    void fiveCountersCreateFiveElementals() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isZero();
        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(5).allSatisfy(elemental -> {
            assertThat(elemental.getCard().getPower()).isEqualTo(3);
            assertThat(elemental.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
        });
    }

    @Test
    @DisplayName("More than five charge counters create and then remove all of them")
    void moreThanFiveCountersCreateThatManyElementals() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 7);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(findPermanents(player1, "Elemental")).hasSize(7);
    }

    @Test
    @DisplayName("Fewer than five charge counters do not create Elementals")
    void fewerThanFiveCountersDoNotCreateElementals() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Created Elementals are exiled at the beginning of the next end step")
    void elementalsAreExiledAtNextEndStep() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(5);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("All Elementals from one upkeep are exiled by one delayed trigger")
    void elementalsAreExiledTogether() {
        harness.addToBattlefield(player1, new LightningCoils());
        findPermanent(player1, "Lightning Coils").setCounterCount(CounterType.CHARGE, 5);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("The charge counter threshold is checked again when the upkeep trigger resolves")
    void fallingBelowThresholdStopsUpkeepEffect() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 5);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        coils.setCounterCount(CounterType.CHARGE, 4);
        resolveAllTriggers();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Counters added before resolution are included in the token count")
    void countsCountersAtResolution() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 5);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        coils.setCounterCount(CounterType.CHARGE, 7);
        resolveAllTriggers();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(7);
    }

    @Test
    @DisplayName("Lightning Coils does not trigger during the opponent's upkeep")
    void opponentUpkeepDoesNotCreateTokens() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 5);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Two simultaneous nontoken creature deaths add two charge counters")
    void simultaneousDeathsAddSeparateCounters() {
        harness.addToBattlefield(player1, new LightningCoils());
        harness.addToBattlefield(player1, new OmegaMyr());
        harness.addToBattlefield(player1, new OmegaMyr());

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Lightning Coils").getCounterCount(CounterType.CHARGE))
                .isEqualTo(2);
        assertThat(countPermanents(player1, "Omega Myr")).isZero();
    }
}
