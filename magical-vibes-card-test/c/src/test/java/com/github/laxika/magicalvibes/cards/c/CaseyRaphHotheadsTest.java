package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseyRaphHotheads.class, GrizzlyBears.class})
class CaseyRaphHotheadsTest extends BaseCardTest {

    private static final String EXILE_MODE =
            "Target player exiles the top card of their library and may play it without paying its mana cost until their next end step";
    private static final String TREASURE_MODE = "Target player creates two Treasure tokens";

    @Test
    @DisplayName("The Treasure mode gives the target player two Treasures")
    void treasureModeGivesTwoTreasuresToTheTargetPlayer() {
        castCasey();
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Both modes target different players, and the exile mode's player may cast the exiled card for free")
    void bothModesTargetDifferentPlayersAndTargetPlayerMayCastExiledCardForFree() {
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));

        castCasey();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiledCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingCardIds()).contains(exiledCard.getId());

        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Both modes can't target the same player")
    void bothModesCannotTargetTheSamePlayer() {
        castCasey();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty library doesn't stop the other player's Treasures")
    void emptyLibraryDoesNotPreventTheOtherPlayersTreasures() {
        harness.setLibrary(player1, List.of());
        castCasey();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(gd.exilePlayPermissionHolders()).isEmpty();
    }

    @Test
    @DisplayName("The permission ends as the target player's next end step begins")
    void exilePermissionExpiresWhenTheTargetPlayersNextEndStepBegins() {
        CaseyRaphHotheads exiledCard = new CaseyRaphHotheads();
        harness.setLibrary(player1, List.of(exiledCard));
        castCasey();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player1.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiledCard.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(exiledCard.getId());
        assertThat(gd.exilePlayWithoutPayingCardIds()).doesNotContain(exiledCard.getId());
    }

    @Test
    @DisplayName("An opponent targeted by the exile mode exiles from their own library and may play that card for free")
    void opponentTargetedByExileModeGetsThePermission() {
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));
        castCasey();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiledCard.getId(), player2.getId());
        assertThat(gd.exilePlayWithoutPayingCardIds()).contains(exiledCard.getId());
    }

    private void castCasey() {
        harness.castFromHand(player1, new CaseyRaphHotheads(), "{4}{R}");
        harness.passBothPriorities();
    }
}
