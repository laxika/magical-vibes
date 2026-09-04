package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.RevokeExilePlayPermissionAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElkinBottle.class, DarkRitual.class, SnowCoveredMountain.class})
class ElkinBottleTest extends BaseCardTest {

    private void addBottleReady() {
        Permanent bottle = harness.addToBattlefieldAndReturn(player1, new ElkinBottle());
        bottle.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Card activateBottleWithTop(Card topCard) {
        addBottleReady();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return topCard;
    }

    @Test
    @DisplayName("Activation exiles the top card with play permission and schedules upkeep revoke")
    void exilesTopCardWithPlayPermission() {
        Card top = activateBottleWithTop(new DarkRitual());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(top.getId()));

        List<RevokeExilePlayPermissionAtNextUpkeep> scheduled =
                gd.getDelayedActions(RevokeExilePlayPermissionAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().cardId()).isEqualTo(top.getId());
    }

    @Test
    @DisplayName("Permission is revoked at the controller's next upkeep but the card stays in exile")
    void permissionRevokedAtControllerUpkeep() {
        Card top = activateBottleWithTop(new DarkRitual());

        advanceToUpkeep(player1);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        // Elkin Bottle never moves the card anywhere — it just stays in exile.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerGraveyards.getOrDefault(player1.getId(), List.of()))
                .noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getDelayedActions(RevokeExilePlayPermissionAtNextUpkeep.class)).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Permission lasts through an opponent's upkeep")
    void permissionLastsThroughOpponentUpkeep() {
        Card top = activateBottleWithTop(new DarkRitual());

        advanceToUpkeep(player2);

        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.getDelayedActions(RevokeExilePlayPermissionAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The controller may cast the exiled spell by paying its normal mana cost")
    void controllerMayCastExiledSpellAtNormalCost() {
        Card top = activateBottleWithTop(new DarkRitual());

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("The controller may play the exiled land")
    void controllerMayPlayExiledLand() {
        Card top = activateBottleWithTop(new SnowCoveredMountain());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, top.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(top.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("Activation with an empty library exiles nothing")
    void emptyLibraryDoesNothing() {
        addBottleReady();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(RevokeExilePlayPermissionAtNextUpkeep.class)).isEmpty();
    }
}
