package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildMongrel.class, Werebear.class})
class WildMongrelTest extends BaseCardTest {

    @Test
    void discardingGivesPlusOnePlusOneAndChosenColorUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        harness.setHand(player1, List.of(new Werebear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, mongrel)).containsExactly(CardColor.RED);
        harness.assertInGraveyard(player1, "Werebear");
    }

    @Test
    void boostAndChosenColorWearOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        harness.setHand(player1, List.of(new Werebear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, mongrel)).containsExactly(CardColor.GREEN);
    }

    @Test
    void eachActivationStacksTheBoostAndLatestColorChoiceApplies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        harness.setHand(player1, List.of(new Werebear(), new Werebear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, mongrel)).containsExactly(CardColor.BLUE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Werebear", "Werebear");
    }

    @Test
    void cannotActivateWithoutACardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
