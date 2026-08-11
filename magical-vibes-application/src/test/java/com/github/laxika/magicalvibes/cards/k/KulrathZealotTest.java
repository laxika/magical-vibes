package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KulrathZealotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with permission to play it until the end of your next turn")
    void etbExilesTopCardWithNextTurnPlayPermission() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new KulrathZealot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCard.getId()))
                .isEqualTo(gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingDiscardsAndSearchesForBasicLand() {
        harness.setHand(player1, List.of(new KulrathZealot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kulrath Zealot");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    @Test
    @DisplayName("Basic landcycling puts the chosen basic land into hand")
    void basicLandcyclingChoosesBasicLand() {
        harness.setHand(player1, List.of(new KulrathZealot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals(chosenName));
    }
}
