package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeneralLeoCristophe.class, GrizzlyBears.class, ThunderingGiant.class})
class GeneralLeoCristopheTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value 3 or less and gets counters for each creature controlled")
    void returnsCreatureAndCountsAllControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        castGeneral();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returnedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        Permanent general = findPermanent(player1, "General Leo Cristophe");
        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The up-to-one return may be declined and still counts creatures")
    void mayDeclineReturn() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        castGeneral();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        Permanent general = findPermanent(player1, "General Leo Cristophe");
        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards with mana value 3 or less are valid return targets")
    void filtersInvalidGraveyardCards() {
        Card tooExpensive = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(tooExpensive));
        castGeneral();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        Permanent general = findPermanent(player1, "General Leo Cristophe");
        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Thundering Giant");
    }

    private void castGeneral() {
        harness.setHand(player1, List.of(new GeneralLeoCristophe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
