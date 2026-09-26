package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.t.TymaretTheMurderKing;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaramirStewardOfGondor.class, CaptainSisay.class, TymaretTheMurderKing.class})
class FaramirStewardOfGondorTest extends BaseCardTest {

    @Test
    void becomesMonarchWhenAControlledLegendaryCreatureWithManaValueFourOrGreaterEnters() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());

        harness.enterBattlefieldAndReturn(player1, new CaptainSisay());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void doesNotTriggerForALegendaryCreatureWithManaValueLessThanFour() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());

        harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void createsTwoHumanSoldierTokensAtTheMonarchsEndStep() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
    }

    @Test
    void doesNotCreateTokensAtTheEndStepWhenItsControllerIsNotTheMonarch() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player2.getId();

        advanceToEndStep(player1);

        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
