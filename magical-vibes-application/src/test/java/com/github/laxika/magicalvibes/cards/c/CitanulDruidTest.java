package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CitanulDruid.class, GrizzlyBears.class, Memnite.class})
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
        harness.setHand(player1, List.of(new Memnite()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCreatureForOpponent(Card card) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(card));
        if (card instanceof GrizzlyBears) {
            harness.addMana(player2, ManaColor.GREEN, 2);
        }
        harness.castCreature(player2, 0);
        resolveAllTriggers();
    }
}
