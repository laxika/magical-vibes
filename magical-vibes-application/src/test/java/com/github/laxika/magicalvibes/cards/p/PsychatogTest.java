package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PsychatogTest extends BaseCardTest {

    @Test
    void discardingACardBoostsPsychatog() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isEqualTo(1);
        assertThat(psychatog.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void exilingTwoGraveyardCardsBoostsPsychatog() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isEqualTo(1);
        assertThat(psychatog.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void cannotExileTwoCardsWithoutTwoCardsInGraveyard() {
        harness.addToBattlefield(player1, new Psychatog());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsWearOffAtEndOfTurn() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isZero();
        assertThat(psychatog.getToughnessModifier()).isZero();
    }
}
