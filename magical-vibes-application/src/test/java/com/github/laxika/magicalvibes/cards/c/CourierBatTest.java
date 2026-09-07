package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourierBat.class, GrizzlyBears.class, Shock.class})
class CourierBatTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when you have not gained life this turn")
    void doesNotTriggerWithoutLifeGain() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castCourierBat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns up to one creature card after life gain")
    void returnsCreatureCardAfterLifeGain() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureCard));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer noncreature cards")
    void doesNotOfferNoncreatureCards() {
        harness.setGraveyard(player1, List.of(new Shock()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Can decline the optional creature return")
    void canDeclineReturn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void castCourierBat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CourierBat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
