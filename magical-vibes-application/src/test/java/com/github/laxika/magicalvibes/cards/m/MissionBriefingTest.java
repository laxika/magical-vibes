package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MissionBriefingTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils before offering only an instant or sorcery from your graveyard")
    void surveilsThenOffersFilteredGraveyardChoice() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Shock shock = new Shock();
        Shock secondShock = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setGraveyard(player1, List.of(shock, creature, secondShock));
        harness.setGraveyard(player2, List.of(new Shock()));

        resolveMissionBriefing(topCard, secondCard);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Casts the chosen graveyard spell later that turn and exiles it")
    void castsChosenSpellLaterAndExilesIt() {
        Shock shock = new Shock();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock));

        resolveMissionBriefing(new GrizzlyBears(), new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("The chosen card remains in the graveyard until cast")
    void chosenCardRemainsInGraveyardUntilCast() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock));

        resolveMissionBriefing(new GrizzlyBears(), new GrizzlyBears());

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(shock.getId()));
    }

    private void resolveMissionBriefing(Card topCard, Card secondCard) {
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new MissionBriefing()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
    }
}
