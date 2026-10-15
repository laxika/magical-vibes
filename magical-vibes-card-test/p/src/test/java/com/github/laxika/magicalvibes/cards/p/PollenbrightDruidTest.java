package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.ManaGeode;
import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollenbrightDruid.class, Snarespinner.class, ManaGeode.class})
class PollenbrightDruidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode puts a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        Permanent spider = addCreatureReady(player2, new Snarespinner());

        castDruid(0, spider.getId());
        resolveAllTriggers();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB mode proliferates")
    void proliferates() {
        Permanent spider = addCreatureReady(player1, new Snarespinner());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castDruid(1);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(spider.getId()));

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counter mode rejects a noncreature target")
    void counterModeRejectsNoncreatureTarget() {
        Permanent geode = harness.addToBattlefieldAndReturn(player2, new ManaGeode());
        castDruid(0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, geode.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Pollenbright Druid").getId());
        resolveAllTriggers();

        assertThat(geode.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Druid cast without ETB choices chooses proliferate after entering")
    void choosesModeAfterEntering() {
        Permanent spider = addCreatureReady(player1, new Snarespinner());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new PollenbrightDruid()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Proliferate");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(spider.getId()));

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Pollenbright Druid")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entering without being cast still offers proliferate")
    void choosesProliferateWhenNotCast() {
        Permanent spider = addCreatureReady(player2, new Snarespinner());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.enterBattlefieldAndReturn(player1, new PollenbrightDruid());
        resolveAllTriggers();

        harness.handleListChoice(player1, "Proliferate");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(spider.getId()));

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The counter mode can target the Druid itself")
    void canPutCounterOnItself() {
        castDruid(0);
        resolveAllTriggers();
        Permanent druid = findPermanent(player1, "Pollenbright Druid");
        harness.handlePermanentChosen(player1, druid.getId());
        resolveAllTriggers();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate may choose no permanents or players")
    void mayChooseNothing() {
        Permanent spider = addCreatureReady(player1, new Snarespinner());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        castDruid(1);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Proliferate adds every existing counter kind to chosen permanents and players")
    void proliferatesAllKindsOnChosenObjects() {
        Permanent spider = addCreatureReady(player1, new Snarespinner());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        spider.setCounterCount(CounterType.REACH, 1);
        Permanent opposingGeode = harness.addToBattlefieldAndReturn(player2, new ManaGeode());
        opposingGeode.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosen = addCreatureReady(player2, new Snarespinner());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        castDruid(1);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(spider.getId(), opposingGeode.getId(), player2.getId()));

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(spider.getCounterCount(CounterType.REACH)).isEqualTo(2);
        assertThat(opposingGeode.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(findPermanent(player1, "Pollenbright Druid")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Proliferate with no counters finishes without creating counters")
    void proliferatesWithNoCounters() {
        castDruid(1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Pollenbright Druid")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castDruid(int mode) {
        castDruid(mode, null);
    }

    private void castDruid(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PollenbrightDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode == 0
                ? "Put a +1/+1 counter on target creature" : "Proliferate");
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }
}
