package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaravanEscort;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeOfHeroes.class, CaravanEscort.class, GlorySeeker.class, Opalescence.class})
class TimeOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts only creatures you control with level counters")
    void boostsOnlyControlledCreaturesWithLevelCounters() {
        harness.addToBattlefield(player1, new TimeOfHeroes());
        Permanent ownLeveled = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent ownUnleveled = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent opposingLeveled = harness.addToBattlefieldAndReturn(player2, new CaravanEscort());

        levelUp(player1, 1);
        levelUp(player2, 0);

        assertThat(gqs.getEffectivePower(gd, ownLeveled)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownLeveled)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownUnleveled)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownUnleveled)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingLeveled)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingLeveled)).isEqualTo(2);
    }

    @Test
    void bonusDoesNotScaleWithLevelCountAndEndsWhenLastCounterIsRemoved() {
        harness.addToBattlefield(player1, new TimeOfHeroes());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());

        levelUp(player1, 1);
        levelUp(player1, 1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        creature.setCounterCount(CounterType.LEVEL, 0);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void boostsNonLevelerWithLevelCounterButNotWithOtherCounter() {
        harness.addToBattlefield(player1, new TimeOfHeroes());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        creature.setCounterCount(CounterType.LEVEL, 1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new TimeOfHeroes());
        harness.addToBattlefield(player1, new TimeOfHeroes());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        levelUp(player1, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    void boostsItselfWhenAnimatedAndBearingLevelCounter() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new TimeOfHeroes());
        harness.addToBattlefield(player1, new Opalescence());
        enchantment.setCounterCount(CounterType.LEVEL, 1);

        assertThat(gqs.getEffectivePower(gd, enchantment)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantment)).isEqualTo(4);
    }

    private void levelUp(Player player, int permanentIndex) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.activateAbility(player, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }
}
