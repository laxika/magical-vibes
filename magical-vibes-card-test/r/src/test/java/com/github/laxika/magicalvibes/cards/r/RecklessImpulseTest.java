package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BelligerentGuest;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessImpulse.class, BelligerentGuest.class, Forest.class})
class RecklessImpulseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards and grants play permission until the end of the next turn")
    void exilesTopTwoCardsAndGrantsPlayPermission() {
        Card first = new BelligerentGuest();
        Card second = new Forest();
        Card remaining = new BelligerentGuest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(first.getId(), Integer.MAX_VALUE)
                .containsEntry(second.getId(), Integer.MAX_VALUE);
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    @DisplayName("Exiles only the cards left when the library has fewer than two cards")
    void exilesWholeLibraryWhenShort() {
        Card only = new BelligerentGuest();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(only);
        assertThat(gd.exilePlayPermissions).containsEntry(only.getId(), player1.getId());
    }

    @Test
    @DisplayName("Play permission expires at the end of the next turn")
    void playPermissionExpiresAtEndOfNextTurn() {
        Card top = new BelligerentGuest();
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May cast an exiled creature by paying its normal mana cost")
    void castsExiledCreatureWithNormalManaCost() {
        Card creature = new BelligerentGuest();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("May play an exiled land, but still only one land per turn")
    void playsExiledLandWithNormalLandLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.castFromExile(player1, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(first.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Permission does not allow casting a creature during the opponent's turn")
    void normalTimingRestrictionsStillApply() {
        Card creature = new BelligerentGuest();
        harness.setLibrary(player1, List.of(creature, new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(creature.getId()));
    }

    @Test
    @DisplayName("An empty library exiles nothing without losing the game")
    void emptyLibraryExilesNothing() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anySatisfy(card -> assertThat(card).isInstanceOf(RecklessImpulse.class));
    }

    @Test
    @DisplayName("Only the caster may play the cards exiled by Reckless Impulse")
    void opponentCannotUsePlayPermission() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player2, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(land.getId()));
    }

    @Test
    @DisplayName("Permission expires after the caster's next turn even when it is an extra turn")
    void permissionExpiresAfterNextExtraTurn() {
        Card top = new BelligerentGuest();
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        gd.queueExtraTurnFirst(player1.getId(), false);

        harness.passUntil(player1, TurnStep.END_STEP);
        int currentTurn = gd.turnNumber;
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.turnNumber).isEqualTo(currentTurn + 1);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }
}
