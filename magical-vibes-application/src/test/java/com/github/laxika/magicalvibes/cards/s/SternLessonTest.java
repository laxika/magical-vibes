package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SternLessonTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, discards one, and creates a tapped Powerstone")
    void drawsDiscardsAndCreatesPowerstone() {
        Card discarded = new GrizzlyBears();
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new SternLesson(), discarded));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);

        List<Permanent> powerstones = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.POWERSTONE))
                .toList();
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
