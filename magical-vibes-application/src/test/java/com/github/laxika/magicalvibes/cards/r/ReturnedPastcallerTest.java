package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VoicelessSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnedPastcaller.class, VoicelessSpirit.class, Shock.class, Divination.class, GrizzlyBears.class})
class ReturnedPastcallerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a Spirit card from the graveyard to hand")
    void returnsSpiritFromGraveyardToHand() {
        VoicelessSpirit spirit = new VoicelessSpirit();
        harness.setGraveyard(player1, List.of(spirit));

        castReturnedPastcaller();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Voiceless Spirit");
        harness.assertNotInGraveyard(player1, "Voiceless Spirit");
    }

    @Test
    @DisplayName("ETB returns an instant or sorcery card from the graveyard to hand")
    void returnsInstantOrSorceryFromGraveyardToHand() {
        Shock shock = new Shock();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(shock, divination));

        castReturnedPastcaller();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB cannot target an unrelated creature card")
    void cannotTargetUnrelatedCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castReturnedPastcaller();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB returns an instant while leaving other eligible cards in the graveyard")
    void returnsInstantFromGraveyardToHand() {
        Shock shock = new Shock();
        ReturnedPastcaller spirit = new ReturnedPastcaller();
        harness.setGraveyard(player1, List.of(shock, spirit));

        castReturnedPastcaller();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Returned Pastcaller");
    }

    @Test
    @DisplayName("ETB cannot return a Spirit from an opponent's graveyard")
    void cannotReturnOpponentsSpirit() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new ReturnedPastcaller()));

        castReturnedPastcaller();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Returned Pastcaller");
        harness.assertNotInHand(player1, "Returned Pastcaller");
    }

    @Test
    @DisplayName("ETB can return another Returned Pastcaller as a Spirit")
    void returnsAnotherPastcaller() {
        ReturnedPastcaller spirit = new ReturnedPastcaller();
        harness.setGraveyard(player1, List.of(spirit));

        castReturnedPastcaller();
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Returned Pastcaller");
        harness.assertNotInGraveyard(player1, "Returned Pastcaller");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void doesNotRetargetWhenTargetLeavesGraveyard() {
        Shock shock = new Shock();
        ReturnedPastcaller spirit = new ReturnedPastcaller();
        harness.setGraveyard(player1, List.of(shock, spirit));

        castReturnedPastcaller();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player1, List.of(spirit));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Shock");
        harness.assertNotInHand(player1, "Returned Pastcaller");
        harness.assertInGraveyard(player1, "Returned Pastcaller");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castReturnedPastcaller() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ReturnedPastcaller()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
