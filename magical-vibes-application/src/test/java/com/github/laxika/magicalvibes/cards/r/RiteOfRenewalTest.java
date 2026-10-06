package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfRenewal.class, GrizzlyBears.class, HolyDay.class, LeoninScimitar.class, IvoryMask.class})
class RiteOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns permanent cards and shuffles target graveyard cards into its library")
    void returnsPermanentsAndShufflesTargetCards() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Card opponentCard = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));

        PendingInteraction.MultiGraveyardChoice shuffleChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(shuffleChoice.validCardIds()).containsExactly(opponentCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player2, "Holy Day");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize + 1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }

    @Test
    @DisplayName("Only permanent cards are legal for the return target group")
    void onlyPermanentCardsCanBeReturned() {
        Card permanent = new GrizzlyBears();
        Card instant = new HolyDay();
        Card opponentCard = new HolyDay();
        harness.setGraveyard(player1, List.of(permanent, instant));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.validCardIds()).containsExactly(permanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Both up-to effects may choose zero cards")
    void bothEffectsMayChooseZeroCards() {
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }

    @Test
    void mayDeclineBothGroupsWithEligibleCardsPresent() {
        Card creature = new GrizzlyBears();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(instant));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Holy Day");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }

    @Test
    void samePermanentMayBeTargetedInBothGroupsButReturnsToHandFirst() {
        Card creature = new GrizzlyBears();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, instant));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Holy Day");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize + 1)
                .contains(instant).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }

    @Test
    void shufflesFourCardsWithoutReturningUnselectedPermanents() {
        Card creature = new GrizzlyBears();
        Card first = new HolyDay();
        Card second = new HolyDay();
        Card third = new HolyDay();
        Card fourth = new HolyDay();
        Card unselected = new HolyDay();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(first, second, third, fourth, unselected));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize + 4)
                .contains(first, second, third, fourth).doesNotContain(unselected);
    }

    @Test
    void stillResolvesWhenAllGraveyardTargetsLeaveButPlayerRemainsLegal() {
        Card creature = new GrizzlyBears();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(instant));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setExile(player1, List.of(creature));
        harness.setExile(player2, List.of(instant));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Rite of Renewal");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }

    @Test
    @CardUsed(IvoryMask.class)
    void returnsLegalPermanentAndExilesItselfWhenTargetPlayerGainsShroud() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.addToBattlefield(player2, new IvoryMask());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Rite of Renewal");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }
}
