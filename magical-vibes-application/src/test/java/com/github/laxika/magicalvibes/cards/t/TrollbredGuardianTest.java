package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrollbredGuardian.class, AxebaneBeast.class})
class TrollbredGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt puts two +1/+1 counters on Trollbred Guardian")
    void adaptPutsTwoCountersOnTrollbredGuardian() {
        Permanent guardian = addGuardian();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have trample")
    void counteredOwnCreaturesHaveTrample() {
        Permanent guardian = addGuardian();
        Permanent creature = addCreatureReady(player1, new AxebaneBeast());
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trollbred Guardian does not grant trample to uncountered or opposing creatures")
    void onlyCounteredOwnCreaturesHaveTrample() {
        addGuardian();
        Permanent uncountered = addCreatureReady(player1, new AxebaneBeast());
        Permanent opponentCreature = addCreatureReady(player2, new AxebaneBeast());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Adapt can be activated once Trollbred Guardian has a +1/+1 counter")
    void adaptCanBeActivatedWithCounter() {
        Permanent guardian = addGuardian();
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptChecksCountersWhenItResolves() {
        Permanent guardian = addGuardian();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptSucceedsIfCountersAreRemovedBeforeResolution() {
        Permanent guardian = addGuardian();
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void adaptWorksWhileTappedAndSummoningSick() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new TrollbredGuardian());
        guardian.setSummoningSick(true);
        guardian.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(guardian.isTapped()).isTrue();
    }

    @Test
    void trampleTracksCountersAndRequiresGuardianOnBattlefield() {
        Permanent guardian = addGuardian();
        Permanent creature = addCreatureReady(player1, new AxebaneBeast());

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(guardian);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addGuardian() {
        return addCreatureReady(player1, new TrollbredGuardian());
    }
}
