package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
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

@CardUsed({ScourgeOfSkolaVale.class, GiantSpider.class})
class ScourgeOfSkolaValeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new ScourgeOfSkolaVale()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scourge = findPermanent(player1, "Scourge of Skola Vale");
        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(scourge.getEffectivePower()).isEqualTo(2);
        assertThat(scourge.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts counters equal to the sacrificed creature's toughness")
    void putsCountersEqualToSacrificedToughness() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfSkolaVale());
        scourge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new GiantSpider());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot sacrifice Scourge of Skola Vale itself")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new ScourgeOfSkolaVale());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesModifiedToughnessAndPaysCostsBeforeResolution() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfSkolaVale());
        scourge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent food = addCreatureReady(player1, new ScourgeOfSkolaVale());
        food.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(scourge.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        harness.assertInGraveyard(player1, "Scourge of Skola Vale");
        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void capturesToughnessOfChosenCreatureWhenSeveralAreAvailable() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfSkolaVale());
        scourge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent smaller = addCreatureReady(player1, new ScourgeOfSkolaVale());
        smaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent larger = addCreatureReady(player1, new ScourgeOfSkolaVale());
        larger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, larger.getId());
        harness.passBothPriorities();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(smaller).doesNotContain(larger);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfSkolaVale());
        scourge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        scourge.setSummoningSick(true);
        Permanent food = addCreatureReady(player1, new ScourgeOfSkolaVale());
        food.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(food);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfSkolaVale());
        scourge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        scourge.setTapped(true);
        Permanent food = addCreatureReady(player1, new ScourgeOfSkolaVale());
        food.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(food);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfSkolaVale());
        scourge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opposingCreature = addCreatureReady(player2, new ScourgeOfSkolaVale());
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
    }
}
