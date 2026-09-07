package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrazenCollector.class, GrizzlyBears.class})
class BrazenCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking adds one red mana")
    void attackingAddsRedMana() {
        addCreatureReady(player1, new BrazenCollector());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger Brazen Collector")
    void anotherCreatureAttackingDoesNotTrigger() {
        addCreatureReady(player1, new BrazenCollector());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Mana from Brazen Collector survives step transitions but ends with the turn")
    void manaSurvivesStepTransitionsUntilEndOfTurn() {
        addCreatureReady(player1, new BrazenCollector());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pool.get(ManaColor.RED)).isZero();
    }
}
