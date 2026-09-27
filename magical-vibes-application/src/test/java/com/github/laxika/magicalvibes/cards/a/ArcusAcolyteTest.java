package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({ArcusAcolyte.class, GrizzlyBears.class})
class ArcusAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Arcus Acolyte can outlast itself")
    void outlastsItself() {
        Permanent acolyte = addCreatureReady(player1, new ArcusAcolyte());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(acolyte.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Arcus Acolyte grants outlast to other counterless creatures you control")
    void grantsOutlastToOtherCounterlessCreatures() {
        Permanent acolyte = addCreatureReady(player1, new ArcusAcolyte());
        Permanent counterlessCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureWithCounter = addCreatureReady(player1, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, acolyte)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, counterlessCreature)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, creatureWithCounter)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingCreature)).isEmpty();

        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(counterlessCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void prepareForSorceryAction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
