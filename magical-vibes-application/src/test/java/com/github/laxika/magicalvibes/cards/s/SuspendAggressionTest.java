package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuspendAggressionTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Card setTopCard(com.github.laxika.magicalvibes.model.Player player) {
        Card top = new Island();
        gd.playerDecks.get(player.getId()).addFirst(top);
        return top;
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Exiles the target permanent and grants its owner play permission")
    void exilesTargetPermanentForItsOwner() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsCardId = bears.getOriginalCard().getId();
        Card top = setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        // Target permanent exiled; opponent (its owner) may play it.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.exilePlayPermissions.get(bearsCardId)).isEqualTo(player2.getId());

        // Top card of caster's library exiled; caster may play it.
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Opponent's card expires at the end of the opponent's upcoming turn")
    void opponentPermissionOwnerRelativeExpiry() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsCardId = bears.getOriginalCard().getId();
        Card top = setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        int turn = gd.turnNumber; // player1's turn
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        // Opponent (player2) is not the active player: their next turn is the very next turn.
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(bearsCardId)).isEqualTo(turn + 1);
        // Caster (player1) is active: their next turn is two turns away.
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(top.getId())).isEqualTo(turn + 2);
    }

    @Test
    @DisplayName("Can target your own permanent, granting yourself play permission")
    void canTargetOwnPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsCardId = bears.getOriginalCard().getId();
        setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions.get(bearsCardId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears()); // a valid target so the spell is playable
        setTopCard(player1);

        harness.setHand(player1, List.of(new SuspendAggression()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }
}
