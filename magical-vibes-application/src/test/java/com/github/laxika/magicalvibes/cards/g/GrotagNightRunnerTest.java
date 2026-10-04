package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrotagNightRunner.class, Mountain.class})
class GrotagNightRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card with end-of-turn play permission")
    void combatDamageExilesTopCardWithPlayPermission() {
        Card topCard = topCard();
        harness.setLibrary(player1, List.of(topCard));
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        Card topCard = topCard();
        harness.setLibrary(player1, List.of(topCard));
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("The exiled creature requires its normal mana cost and normal casting timing")
    void exiledCreatureRequiresNormalCostAndTiming() {
        Card top = new GrotagNightRunner();
        exileThroughCombat(top);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).clear();
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, top.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(top.getId()));
    }

    @Test
    @DisplayName("An exiled land can be played but cannot bypass the land-play limit")
    void exiledLandUsesNormalLandPlayLimit() {
        Card top = new Mountain();
        exileThroughCombat(top);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.castFromExile(player1, top.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(top.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the trigger controller may play the exiled card")
    void opponentCannotPlayExiledCard() {
        Card top = new GrotagNightRunner();
        exileThroughCombat(top);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromExile(player2, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An empty library does not exile a card or make its controller lose")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Removing the source before resolution does not stop the exile ability")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Card top = new GrotagNightRunner();
        harness.setLibrary(player1, List.of(top));
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(runner);
        harness.setGraveyard(player1, List.of(runner.getCard()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not exile a card")
    void blockedRunnerDoesNotExile() {
        Card top = new GrotagNightRunner();
        harness.setLibrary(player1, List.of(top));
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrotagNightRunner());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
    private void exileThroughCombat(Card top) {
        harness.setLibrary(player1, List.of(top));
        Permanent runner = addCreatureReady(player1, new GrotagNightRunner());
        runner.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
    }
    private Card topCard() {
        Card card = new Card();
        card.setType(CardType.INSTANT);
        return card;
    }
}
