package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurrakAndGoreclaw.class, GrizzlyBears.class})
class SurrakAndGoreclawTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature entering gets a +1/+1 counter, haste, and trample")
    void buffsAnotherNontokenCreatureEntering() {
        harness.addToBattlefield(player1, new SurrakAndGoreclaw());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The haste grant wears off at end of turn but the counter remains")
    void hasteWearsOffButCounterRemains() {
        harness.addToBattlefield(player1, new SurrakAndGoreclaw());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new SurrakAndGoreclaw());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed(com.github.laxika.magicalvibes.cards.d.DeadlyInsect.class)
    void enteringCreatureWithShroudReceivesCounterAndHasteInOneAbility() {
        harness.addToBattlefield(player1, new SurrakAndGoreclaw());
        Permanent creature = harness.enterBattlefieldAndReturn(player1,
                new com.github.laxika.magicalvibes.cards.d.DeadlyInsect());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }
}
