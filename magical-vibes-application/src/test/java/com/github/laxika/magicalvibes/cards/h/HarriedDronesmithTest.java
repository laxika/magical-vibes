package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarriedDronesmith.class})
class HarriedDronesmithTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty 1/1 colorless Thopter artifact creature token at the beginning of your combat")
    void createsHastyThopterAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new HarriedDronesmith());

        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new HarriedDronesmith());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices the token at the beginning of the next end step")
    void sacrificesTokenAtNextEndStep() {
        harness.addToBattlefield(player1, new HarriedDronesmith());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);

        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
