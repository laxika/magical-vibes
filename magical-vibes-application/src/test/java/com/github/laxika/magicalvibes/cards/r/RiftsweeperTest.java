package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({Riftsweeper.class, Shock.class})
class RiftsweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles a face-up exiled card into its owner's library")
    void shufflesFaceUpExiledCardIntoOwnersLibrary() {
        Shock exiledCard = new Shock();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ETBExiledCardTargetChoice.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(exiledCard.getId())));

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).contains(exiledCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Does not target a face-down exiled card")
    void doesNotTargetFaceDownExiledCard() {
        Card exiledCard = new Shock();
        gd.addToExile(player2.getId(), exiledCard, null, true);
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(exiledCard);
    }
}
