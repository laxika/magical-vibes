package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.RevokeExilePlayPermissionAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElkinBottleTest extends BaseCardTest {

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }

    private void addBottleReady() {
        harness.addToBattlefield(player1, new ElkinBottle());
        Permanent bottle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elkin Bottle"))
                .findFirst().orElseThrow();
        bottle.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Card putOnTop(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player1.getId()).addFirst(card);
        return card;
    }

    @Test
    @DisplayName("Activation exiles the top card with play permission and schedules upkeep revoke")
    void exilesTopCardWithPlayPermission() {
        addBottleReady();
        Card top = putOnTop("Exiled Spell");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

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
        addBottleReady();
        Card top = putOnTop("Unplayed Spell");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd.activePlayerId = player1.getId();
        stepTriggerService().handleUpkeepTriggers(gd);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        // Elkin Bottle never moves the card anywhere — it just stays in exile.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerGraveyards.getOrDefault(player1.getId(), List.of()))
                .noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getDelayedActions(RevokeExilePlayPermissionAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Permission lasts through an opponent's upkeep")
    void permissionLastsThroughOpponentUpkeep() {
        addBottleReady();
        Card top = putOnTop("Still Playable Spell");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd.activePlayerId = player2.getId();
        stepTriggerService().handleUpkeepTriggers(gd);

        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.getDelayedActions(RevokeExilePlayPermissionAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Activation with an empty library exiles nothing")
    void emptyLibraryDoesNothing() {
        addBottleReady();
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(RevokeExilePlayPermissionAtNextUpkeep.class)).isEmpty();
    }
}
