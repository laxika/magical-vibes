package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArdentDustspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can put an instant or sorcery on the bottom and exile the top two cards")
    void attackPutsSpellOnBottomAndExilesTopTwo() {
        Card instant = new Shock();
        Card sorcery = new Duress();
        Card nonMatching = new GrizzlyBears();
        Card topCard = new LlanowarElves();
        Card secondCard = new GrizzlyBears();
        Card cardBelowExiledCards = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(instant, sorcery, nonMatching));
        harness.setLibrary(player1, List.of(topCard, secondCard, cardBelowExiledCards));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId(), sorcery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, nonMatching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cardBelowExiledCards, sorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(topCard, secondCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCard.getId(), player1.getId())
                .containsEntry(secondCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(topCard.getId(), secondCard.getId());
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the graveyard and library unchanged")
    void decliningLeavesZonesUnchanged() {
        Card instant = new Shock();
        Card topCard = new LlanowarElves();
        Card cardBelow = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(instant);
        harness.setLibrary(player1, List.of(topCard, cardBelow));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, cardBelow);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting without an instant or sorcery does not exile cards")
    void noMatchingGraveyardCardDoesNotExile() {
        Card creature = new GrizzlyBears();
        Card topCard = new LlanowarElves();
        gd.playerGraveyards.get(player1.getId()).add(creature);
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
