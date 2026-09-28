package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeideggerShinraExecutive.class, GrizzlyBears.class, YotianSoldier.class})
class HeideggerShinraExecutiveTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureByTheNumberOfSoldiersYouControl() {
        Permanent heidegger = addCreatureReady(player1, new HeideggerShinraExecutive());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new YotianSoldier());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(heidegger.getId(), target.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void createsOneSoldierForEachOpponentWithMoreCreatures() {
        harness.addToBattlefield(player1, new HeideggerShinraExecutive());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    void createsNoSoldiersWhenNoOpponentControlsMoreCreatures() {
        harness.addToBattlefield(player1, new HeideggerShinraExecutive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
