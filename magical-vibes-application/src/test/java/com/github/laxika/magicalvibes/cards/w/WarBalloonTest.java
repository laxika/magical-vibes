package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarBalloon.class, GrizzlyBears.class})
class WarBalloonTest extends BaseCardTest {

    @Test
    @DisplayName("The fire-counter ability costs one mana and adds a fire counter")
    void putsOnFireCounter() {
        Permanent balloon = addWarBalloonReady();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(balloon.getCounterCount(CounterType.FIRE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Three fire counters make War Balloon an artifact creature")
    void becomesCreatureAtThreeFireCounters() {
        Permanent balloon = addWarBalloonReady();

        balloon.setCounterCount(CounterType.FIRE, 2);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();

        balloon.setCounterCount(CounterType.FIRE, 3);
        assertThat(gqs.isCreature(gd, balloon)).isTrue();
        assertThat(gqs.isArtifact(balloon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, balloon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, balloon)).isEqualTo(3);

        balloon.setCounterCount(CounterType.FIRE, 2);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();
    }

    @Test
    @DisplayName("Crew 3 animates War Balloon until end of turn")
    void crewAnimatesUntilEndOfTurn() {
        Permanent balloon = addWarBalloonReady();
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, balloon)).isTrue();
        assertThat(firstBear.isTapped()).isTrue();
        assertThat(secondBear.isTapped()).isTrue();
        assertThat(balloon.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, balloon)).isFalse();
    }

    private Permanent addWarBalloonReady() {
        return addCreatureReady(player1, new WarBalloon());
    }
}
