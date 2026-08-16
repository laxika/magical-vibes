package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftgearDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a card from your graveyard on the bottom of its owner's library")
    void entersAndTucksCardFromOwnGraveyard() {
        Card target = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        castDrake();

        chooseTarget(target);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(target);
    }

    @Test
    @DisplayName("ETB can put a card from an opponent's graveyard on the bottom of its owner's library")
    void entersAndTucksCardFromOpponentGraveyard() {
        Card target = new Shock();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest())));
        castDrake();

        chooseTarget(target);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getLast()).isSameAs(target);
    }

    @Test
    @DisplayName("ETB may decline to put a card on the bottom of a library")
    void mayDeclineTarget() {
        Card target = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        castDrake();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castDrake() {
        harness.setHand(player1, List.of(new SwiftgearDrake()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseTarget(Card target) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
