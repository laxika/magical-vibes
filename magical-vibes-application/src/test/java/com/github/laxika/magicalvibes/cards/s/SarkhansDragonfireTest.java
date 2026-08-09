package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SarkhansDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage, then offers one red card from the top five")
    void dealsDamageAndOffersRedCard() {
        Card redCard = new Shock();
        Card greenCard = new LlanowarElves();
        Card blueCard = new Island();
        Card whiteCard = new Plains();
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(redCard, greenCard, blueCard, whiteCard, creature));

        harness.setHand(player1, List.of(new SarkhansDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        PendingInteraction.LibraryRevealChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(redCard.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.CardsChosen(List.of(redCard.getId())));

        harness.assertInHand(player1, "Shock");
        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameData.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(greenCard, blueCard, whiteCard, creature);
        harness.assertInGraveyard(player1, "Sarkhan's Dragonfire");
    }
}
