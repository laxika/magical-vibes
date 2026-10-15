package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.f.Flatten;
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

@CardUsed({SunscorchRegent.class, DragonScarredBear.class, Flatten.class})
class SunscorchRegentTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a spell puts a +1/+1 counter on Sunscorch Regent and gains you 1 life")
    void opponentCastingSpellAddsCounterAndLife() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new SunscorchRegent());
        prepareOpponentMainPhase();
        harness.castFromHand(player2, new DragonScarredBear(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Your own spell does not trigger Sunscorch Regent")
    void ownSpellDoesNotTrigger() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new SunscorchRegent());
        harness.castFromHand(player1, new DragonScarredBear(), "{2}{G}");

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each opponent spell triggers independently before that spell resolves")
    void repeatedOpponentSpellsEachAddCounterAndLife() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new SunscorchRegent());
        prepareOpponentMainPhase();

        for (int count = 1; count <= 2; count++) {
            harness.castFromHand(player2, new DragonScarredBear(), "{2}{G}");
            assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(count - 1);
            harness.passBothPriorities();
            assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(count);
            harness.assertLife(player1, 20 + count);
            harness.assertLife(player2, 20);
            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(count - 1);
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("An opponent's instant triggers Regent during its controller's turn")
    void opponentInstantTriggersDuringControllerTurn() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new SunscorchRegent());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());
        harness.setHand(player2, List.of(new Flatten()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger still gains life when Regent dies before it resolves")
    void gainsLifeWhenSourceLeavesBeforeResolution() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new SunscorchRegent());
        prepareOpponentMainPhase();
        harness.castFromHand(player2, new DragonScarredBear(), "{2}{G}");
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, regent.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(regent);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
