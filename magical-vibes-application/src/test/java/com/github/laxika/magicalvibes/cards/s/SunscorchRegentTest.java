package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunscorchRegent.class, GrizzlyBears.class})
class SunscorchRegentTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a spell puts a +1/+1 counter on Sunscorch Regent and gains you 1 life")
    void opponentCastingSpellAddsCounterAndLife() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new SunscorchRegent());
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Your own spell does not trigger Sunscorch Regent")
    void ownSpellDoesNotTrigger() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new SunscorchRegent());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
