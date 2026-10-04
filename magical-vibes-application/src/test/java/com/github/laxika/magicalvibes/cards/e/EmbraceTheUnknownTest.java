package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbraceTheUnknown.class, Forest.class, GrizzlyBears.class})
class EmbraceTheUnknownTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards and grants permission to play them")
    void exilesTopTwoCardsAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EmbraceTheUnknown()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(first.getId(), gd.turnNumber + 2)
                .containsEntry(second.getId(), gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Retrace discards a land and returns Embrace the Unknown to the graveyard")
    void retraceDiscardsLandAndReturnsToGraveyard() {
        Card embrace = new EmbraceTheUnknown();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(embrace));
        harness.setHand(player1, List.of(land));
        addMana();

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(embrace, land);
    }

    @Test
    void canPlayExiledLandAndCastCreatureByPayingItsCost() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, creature));
        harness.setHand(player1, List.of(new EmbraceTheUnknown()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void exiledLandsStillObeyLandPlayLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EmbraceTheUnknown()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void permissionLastsThroughNextTurnThenExpiresWithoutMovingCards() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, creature, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EmbraceTheUnknown()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.exilePlayPermissions).containsEntry(creature.getId(), player1.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getId());
    }

    @Test
    void shortLibraryExilesOnlyAvailableCard() {
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new EmbraceTheUnknown()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        harness.castFromExile(player1, card.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void emptyLibraryDoesNotPreventResolution() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EmbraceTheUnknown()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Embrace the Unknown");
    }

    @Test
    void retraceRejectsNonlandDiscardWithoutMovingCards() {
        Card embrace = new EmbraceTheUnknown();
        Card nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(embrace));
        harness.setHand(player1, List.of(nonland));
        addMana();

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(embrace);
    }

    @Test
    void retraceStillRequiresNormalManaCost() {
        Card embrace = new EmbraceTheUnknown();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(embrace));
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(embrace);
    }

    @Test
    void canRetraceAgainAfterResolving() {
        Card embrace = new EmbraceTheUnknown();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setGraveyard(player1, List.of(embrace));
        harness.setHand(player1, List.of(firstLand, secondLand));
        addMana();
        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        addMana();
        int embraceIndex = gd.playerGraveyards.get(player1.getId()).indexOf(embrace);
        harness.castRetrace(player1, embraceIndex, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(embrace, firstLand, secondLand);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
