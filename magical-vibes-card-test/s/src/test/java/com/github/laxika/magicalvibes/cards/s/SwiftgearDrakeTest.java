package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftgearDrake.class, Disfigure.class, Forest.class})
class SwiftgearDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a card from your graveyard on the bottom of its owner's library")
    void entersAndTucksCardFromOwnGraveyard() {
        Card target = new Disfigure();
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
        Card target = new Disfigure();
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
        Card target = new Disfigure();
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

    @Test
    @DisplayName("ETB resolves without a target when both graveyards are empty")
    void entersWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castDrake();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Swiftgear Drake");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB can target a land and moves only the chosen card")
    void tucksOnlyChosenLandAndPreservesLibraryOrder() {
        Card target = new Forest();
        Card unchosen = new Disfigure();
        Card top = new Forest();
        Card bottom = new SwiftgearDrake();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target, unchosen)));
        harness.setLibrary(player2, List.of(top, bottom));

        castDrake();
        chooseTarget(target);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unchosen);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, bottom, target);
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void doesNothingWhenTargetLeavesGraveyardBeforeResolution() {
        Card target = new Disfigure();
        Card other = new Forest();
        Card libraryCard = new Forest();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target, other)));
        harness.setLibrary(player2, List.of(libraryCard));
        castDrake();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player2, List.of(other));
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    private void castDrake() {
        harness.castFromHand(player1, new SwiftgearDrake(), "{5}");
        harness.passBothPriorities();
    }

    private void chooseTarget(Card target) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
