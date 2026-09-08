package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void prepareGraveyardAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SurvivorOfKorlis()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
