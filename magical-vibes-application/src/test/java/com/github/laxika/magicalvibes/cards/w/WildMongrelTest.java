package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WildMongrelTest extends BaseCardTest {

    @Test
    void discardingGivesPlusOnePlusOneAndChosenColorUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, mongrel)).containsExactly(CardColor.RED);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void boostAndChosenColorWearOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, mongrel)).containsExactly(CardColor.GREEN);
    }
}
