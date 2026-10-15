package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InvasionReinforcements.class)
class InvasionReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Ally token")
    void etbCreatesAllyToken() {
        harness.castFromHand(player1, new InvasionReinforcements(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCard().isToken()).isTrue();
        assertThat(ally.getCard().getPower()).isEqualTo(1);
        assertThat(ally.getCard().getToughness()).isEqualTo(1);
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).contains(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("The Ally is created only when the enters trigger resolves")
    void tokenCreationUsesTheStack() {
        harness.castFromHand(player1, new InvasionReinforcements(), "{1}{W}");

        assertThat(findPermanents(player1, "Ally")).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Invasion Reinforcements");
        assertThat(findPermanents(player1, "Ally")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        assertThat(findPermanents(player2, "Ally")).isEmpty();
        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.isTapped()).isFalse();
        assertThat(ally.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new InvasionReinforcements(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Invasion Reinforcements");
        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        assertThat(findPermanents(player2, "Ally")).isEmpty();
    }

}
