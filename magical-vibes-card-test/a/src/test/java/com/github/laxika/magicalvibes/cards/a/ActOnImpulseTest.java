package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ActOnImpulse.class, Mountain.class, Ornithopter.class})
class ActOnImpulseTest extends BaseCardTest {

    private void castActOnImpulse(Player player) {
        harness.castFromHand(player, new ActOnImpulse(), "{2}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles the top three cards with end-of-turn play permission")
    void exilesTopThreeWithPlayPermission() {
        Card first = new ActOnImpulse();
        Card second = new ActOnImpulse();
        Card third = new ActOnImpulse();
        Card fourth = new ActOnImpulse();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castActOnImpulse(player1);

        for (Card card : List.of(first, second, third)) {
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getId().equals(card.getId()));
            assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(card.getId());
            // Normal costs and timing — never a free play.
            assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(card.getId());
        }
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Exiles only what is left when the library has fewer than three cards")
    void exilesWholeLibraryWhenShort() {
        Card only = new ActOnImpulse();
        harness.setLibrary(player1, List.of(only));

        castActOnImpulse(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsKey(only.getId());
    }

    @Test
    @DisplayName("Play permission is cleared during end-of-turn cleanup")
    void permissionExpiresAtEndOfTurn() {
        Card first = new ActOnImpulse();
        harness.setLibrary(player1, List.of(first, new ActOnImpulse(), new ActOnImpulse()));

        castActOnImpulse(player1);
        assertThat(gd.exilePlayPermissions).containsKey(first.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
    }

    @Test
    void emptyLibraryExilesNothing() {
        harness.setLibrary(player1, List.of());

        castActOnImpulse(player1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        Card exiled = new ActOnImpulse();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);
        gd.playerManaPools.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiled);
    }

    @Test
    void exiledCreatureCanBeCastDuringMainPhase() {
        Card exiled = new Ornithopter();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
    }

    @Test
    void exiledCreatureStillRequiresNormalTiming() {
        Card exiled = new Ornithopter();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void exiledLandUsesTheAvailableLandPlay() {
        Card exiled = new Mountain();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);

        harness.castFromExile(player1, exiled.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
    }

    @Test
    void exiledLandCannotExceedTheLandPlayLimit() {
        Card exiled = new Mountain();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void exiledLandCannotBePlayedOutsideAMainPhase() {
        Card exiled = new Mountain();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void exiledLandCannotBePlayedWhileTheStackIsNotEmpty() {
        Card exiled = new Mountain();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);
        harness.castFromHand(player1, new Ornithopter(), "{0}");

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void unplayedCardsRemainExiledAfterPermissionExpires() {
        Card exiled = new Ornithopter();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void opponentCannotUseThePlayPermission() {
        Card exiled = new Ornithopter();
        harness.setLibrary(player1, List.of(exiled));
        castActOnImpulse(player1);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }
}
