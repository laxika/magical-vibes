package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForeverYoung.class, GiantSpider.class, GrizzlyBears.class, HolyDay.class})
class ForeverYoungTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any number of target creature cards on top and draws a card")
    void putsSelectedCreaturesOnTopAndDraws() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GiantSpider();
        Card nonCreature = new HolyDay();
        Card spell = new ForeverYoung();
        harness.setGraveyard(player1, List.of(creature1, creature2, nonCreature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(nonCreature, spell);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature1.getId()) || card.getId().equals(creature2.getId()));
    }

    @Test
    @DisplayName("Choosing zero creature cards still draws a card")
    void choosingZeroCardsStillDraws() {
        Card creature = new GrizzlyBears();
        Card topCard = new GiantSpider();
        Card spell = new ForeverYoung();
        harness.setGraveyard(player1, List.of(creature));
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Only creature cards in your graveyard can be selected")
    void onlyCreatureCardsCanBeSelected() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setHand(player1, List.of(new ForeverYoung()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of());

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }
}
