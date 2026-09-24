package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DragonscaleBoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighScore.class, DragonscaleBoon.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class HighScoreTest extends BaseCardTest {

    @Test
    void addsAnAdditionalPlusOnePlusOneCounterToYourCreature() {
        harness.addToBattlefield(player1, new HighScore());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonscaleBoon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void drawsAtYourEndStepWhenYouControlTheGreatestPowerCreature() {
        harness.addToBattlefield(player1, new HighScore());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void drawsAtYourEndStepWhenGreatestPowerIsTied() {
        harness.addToBattlefield(player1, new HighScore());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void doesNotDrawWhenAnOpponentsCreatureHasGreaterPower() {
        harness.addToBattlefield(player1, new HighScore());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
