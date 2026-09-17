package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaxterFlyInTheOintment.class, GrizzlyBears.class})
class BaxterFlyInTheOintmentTest extends BaseCardTest {

    @Test
    @DisplayName("Entering gives flying to your countered creatures until end of turn")
    void enteringGivesFlyingToCounteredCreatures() {
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.CHARGE, 1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.CHARGE, 1);

        castBaxter();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Attacking gives flying to your countered creatures until end of turn")
    void attackingGivesFlyingToCounteredCreatures() {
        Permanent baxter = addCreatureReady(player1, new BaxterFlyInTheOintment());
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.CHARGE, 1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(baxter)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Drawing puts a +1/+1 counter on Baxter, but an opponent's draw does not")
    void drawsPutCountersOnlyForController() {
        Permanent baxter = harness.addToBattlefieldAndReturn(player1, new BaxterFlyInTheOintment());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(baxter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceToDraw(player2);

        assertThat(baxter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castBaxter() {
        harness.setHand(player1, List.of(new BaxterFlyInTheOintment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
