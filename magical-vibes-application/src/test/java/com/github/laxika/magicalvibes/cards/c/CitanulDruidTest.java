package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryTower;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CitanulDruid.class, GrizzlyBears.class, IvoryTower.class, Memnite.class, Ornithopter.class})
class CitanulDruidTest extends BaseCardTest {

    @Test
    void putsCounterOnItselfWhenOpponentCastsArtifactSpell() {
        Permanent druid = addCreatureReady(player1, new CitanulDruid());

        castCreatureForOpponent(new Memnite());

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOpponentNonartifactSpell() {
        Permanent druid = addCreatureReady(player1, new CitanulDruid());

        castCreatureForOpponent(new GrizzlyBears());

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForControllerArtifactSpell() {
        Permanent druid = addCreatureReady(player1, new CitanulDruid());
        harness.castFromHand(player1, new Memnite(), "{0}");
        resolveAllTriggers();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCreatureForOpponent(Card card) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, card, card instanceof GrizzlyBears ? "{1}{G}" : "{0}");
        resolveAllTriggers();
    }

    @Test
    void counterResolvesBeforeOpponentsNoncreatureArtifactSpell() {
        Permanent druid = addCreatureReady(player1, new CitanulDruid());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new IvoryTower(), "{1}");

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player2, "Ivory Tower")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanent(player2, "Ivory Tower")).isNotNull();
        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachDruidGetsOneCounterForEachOpponentArtifactSpell() {
        Permanent firstDruid = addCreatureReady(player1, new CitanulDruid());
        Permanent secondDruid = addCreatureReady(player1, new CitanulDruid());
        Permanent castersDruid = addCreatureReady(player2, new CitanulDruid());

        castCreatureForOpponent(new Ornithopter());
        castCreatureForOpponent(new Ornithopter());

        assertThat(firstDruid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondDruid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(castersDruid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenOpponentArtifactEntersWithoutBeingCast() {
        Permanent druid = addCreatureReady(player1, new CitanulDruid());

        harness.enterBattlefieldAndReturn(player2, new Ornithopter());
        resolveAllTriggers();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
