package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxOfTheSecondSun.class, GrizzlyBears.class})
class SphinxOfTheSecondSunTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an additional beginning phase after postcombat main")
    void createsAdditionalBeginningPhaseAfterPostcombatMain() {
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }
}
