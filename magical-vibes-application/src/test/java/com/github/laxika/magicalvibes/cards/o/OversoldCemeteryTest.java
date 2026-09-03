package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OversoldCemetery.class, GrizzlyBears.class, HolyDay.class})
class OversoldCemeteryTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers at upkeep with four creature cards in the graveyard")
    void triggersWithFourCreatureCards() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Returns the chosen creature card to hand")
    void returnsChosenCreatureToHand() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(
                new HolyDay(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), target));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Day");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Does not trigger with fewer than four creature cards")
    void doesNotTriggerBelowThreshold() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new HolyDay()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Can decline the optional graveyard target")
    void canDeclineTarget() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards are legal graveyard targets")
    void onlyCreatureCardsAreTargets() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new HolyDay(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isInstanceOf(
                PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).extracting(Card::getName).containsOnly("Grizzly Bears");
    }
}
