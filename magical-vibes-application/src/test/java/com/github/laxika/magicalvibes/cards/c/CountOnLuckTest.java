package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CountOnLuck.class, Island.class})
class CountOnLuckTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, exiles the top card with play permission")
    void exilesTopCardWithPlayPermission() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Play permission expires at the end of the turn")
    void playPermissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
    }

    @Test
    @DisplayName("Each upkeep trigger exiles exactly the top card")
    void exilesOnlyOneCard() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        Card next = new Island();
        harness.setLibrary(player1, List.of(top, next));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
    }

    @Test
    @DisplayName("An exiled land can be played in the main phase but not during upkeep")
    void playsExiledLandWithNormalTiming() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Permission does not grant an additional land play")
    void doesNotGrantAdditionalLandPlay() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(countPermanents(player1, "Island")).isEqualTo(1);
    }

    @Test
    @DisplayName("An exiled spell requires its normal mana cost and timing")
    void castsExiledSpellAtNormalCostAndTiming() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new CountOnLuck();
        harness.setLibrary(player1, List.of(top));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).clear();
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Count on Luck")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("An empty library leaves nothing to exile and does not cause a loss")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new CountOnLuck());
        harness.setLibrary(player1, List.of());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
