package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CryptFeasterTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a threshold boost with fewer than seven cards in its controller's graveyard")
    void doesNotBoostBelowThreshold() {
        Permanent feaster = addCreatureReady(player1, new CryptFeaster());
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(feaster.getPowerModifier()).isEqualTo(0);
        assertThat(feaster.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+0 when it attacks with threshold")
    void boostsAtThreshold() {
        Permanent feaster = addCreatureReady(player1, new CryptFeaster());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(feaster.getPowerModifier()).isEqualTo(2);
        assertThat(feaster.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        Permanent feaster = addCreatureReady(player1, new CryptFeaster());
        harness.setGraveyard(player2, graveyardWithSevenCards());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(feaster.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Threshold boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent feaster = addCreatureReady(player1, new CryptFeaster());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(feaster.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(feaster.getPowerModifier()).isEqualTo(0);
        assertThat(feaster.getToughnessModifier()).isEqualTo(0);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new CryptFeaster(), new CryptFeaster(), new CryptFeaster(), new CryptFeaster(),
                new CryptFeaster(), new CryptFeaster(), new CryptFeaster());
    }
}
