package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BatheInGold;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungRedDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrakkusTheButcher.class, YoungRedDragon.class, BatheInGold.class, GrizzlyBears.class})
class ThrakkusTheButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking doubles the current power of each Dragon you control")
    void attackingDoublesControlledDragonsPower() {
        Permanent thrakkus = addCreatureReady(player1, new ThrakkusTheButcher());
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        Permanent nonDragon = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentDragon = addCreatureReady(player2, new YoungRedDragon());

        dragon.setPowerModifier(2);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, nonDragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentDragon)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Dragon power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent thrakkus = addCreatureReady(player1, new ThrakkusTheButcher());
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(3);
    }
}
