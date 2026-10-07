package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurvivorOfKorlis.class})
class SurvivorOfKorlisTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles Survivor of Korlis and scries two")
    void graveyardAbilityExilesAndScriesTwo() {
        prepareGraveyardAbility();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Survivor of Korlis");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Survivor of Korlis"));

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    @DisplayName("Graveyard ability can put both scried cards on the bottom")
    void graveyardAbilityCanBottomBothCards() {
        prepareGraveyardAbility();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card first = deck.get(0);
        Card second = deck.get(1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(deck.get(deck.size() - 2)).isSameAs(first);
        assertThat(deck.get(deck.size() - 1)).isSameAs(second);
    }

    @Test
    @DisplayName("Graveyard ability requires one generic and one white mana")
    void graveyardAbilityRequiresMana() {
        harness.setGraveyard(player1, List.of(new SurvivorOfKorlis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scry can reorder both cards on top without changing the rest of the library")
    void canReorderBothCardsOnTop() {
        prepareGraveyardAbility();
        Card first = new SurvivorOfKorlis();
        Card second = new SurvivorOfKorlis();
        Card third = new SurvivorOfKorlis();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
    }

    @Test
    @DisplayName("Scry can keep one card and put the other below the rest of the library")
    void canSplitCardsBetweenTopAndBottom() {
        prepareGraveyardAbility();
        Card first = new SurvivorOfKorlis();
        Card second = new SurvivorOfKorlis();
        Card third = new SurvivorOfKorlis();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("Scry two with one card in the library looks at only that card")
    void canScryWithOneCardInLibrary() {
        prepareGraveyardAbility();
        Card onlyCard = new SurvivorOfKorlis();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("The graveyard ability resolves with an empty library")
    void canScryWithEmptyLibrary() {
        prepareGraveyardAbility();
        harness.setLibrary(player1, List.of());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertNotInGraveyard(player1, "Survivor of Korlis");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Survivor of Korlis"));
    }

    @Test
    @DisplayName("Two generic mana cannot pay the white part of the activation cost")
    void cannotActivateWithoutWhiteMana() {
        harness.setGraveyard(player1, List.of(new SurvivorOfKorlis()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Survivor of Korlis");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareGraveyardAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SurvivorOfKorlis()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
