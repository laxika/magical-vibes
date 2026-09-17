package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindcallerAven.class, GrizzlyBears.class})
class WindcallerAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling queues a target creature choice and draws a card")
    void cyclingQueuesTargetChoiceAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInGraveyard(player1, "Windcaller Aven");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    void cyclingWithoutALegalTargetStillDraws() {
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingCannotTargetAPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
