package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlaniasPathmaker.class, Shock.class, Mountain.class})
class AlaniasPathmakerTest extends BaseCardTest {

    @Test
    void entersAndGrantsPlayPermissionUntilEndOfNextTurn() {
        Card topCard = new Shock();
        castPathmaker(topCard);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void mayPlayTheExiledCardForItsNormalCost() {
        Card topCard = new Shock();
        castPathmaker(topCard);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void exilesOnlyTheTopCard() {
        Card topCard = new Mountain();
        Card secondCard = new Mountain();
        castPathmaker(topCard, secondCard);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    void emptyLibraryDoesNotPreventEntering() {
        castPathmaker();

        harness.assertOnBattlefield(player1, "Alania's Pathmaker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayPlayAnExiledLand() {
        Card topCard = new Mountain();
        castPathmaker(topCard);

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.landsPlayedThisTurn).containsEntry(player1.getId(), 1);
    }

    @Test
    void exiledLandStillUsesTheNormalLandPlayLimit() {
        Card topCard = new Mountain();
        castPathmaker(topCard);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.landsPlayedThisTurn).containsEntry(player1.getId(), 1);
    }

    @Test
    void cannotPlayExiledLandDuringOpponentsTurn() {
        Card topCard = new Mountain();
        castPathmaker(topCard, new Mountain());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void cannotPlayExiledLandDuringEndStep() {
        Card topCard = new Mountain();
        castPathmaker(topCard);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void cannotCastWithoutPayingMana() {
        Card topCard = new Shock();
        castPathmaker(topCard);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.assertLife(player2, 20);
    }

    @Test
    void canCastTheExiledCardDuringNextTurnsEndStep() {
        Card topCard = new Shock();
        castPathmaker(topCard, new Mountain(), new Mountain());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.interaction.clearAwaitingInput();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void permissionLastsThroughNextTurnAndThenExpiresLeavingCardExiled() {
        Card topCard = new Shock();
        castPathmaker(topCard, new Mountain(), new Mountain(), new Mountain());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.interaction.clearAwaitingInput();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPathmaker(Card... topCards) {
        harness.setLibrary(player1, List.of(topCards));
        harness.setHand(player1, List.of(new AlaniasPathmaker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
