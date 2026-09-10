package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhangLiaoHeroOfHefei.class, ShuFootSoldiers.class, ViridianLongbow.class})
class ZhangLiaoHeroOfHefeiTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to an opponent makes that opponent discard a card of their choice")
    void combatDamageMakesDamagedPlayerDiscard() {
        addAttackingZhangLiao(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new ShuFootSoldiers(), new ShuFootSoldiers())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No trigger when Zhang Liao is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingZhangLiao(player1);
        Permanent blocker = addCreatureReady(player2, new ShuFootSoldiers());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new ShuFootSoldiers())));

        resolveCombatAndTrigger();

        // No combat damage reached the player, so no discard was prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent makes that player discard a card")
    void noncombatDamageMakesDamagedOpponentDiscard() {
        Permanent zhangLiao = addCreatureReady(player1, new ZhangLiaoHeroOfHefei());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(zhangLiao.getId());
        harness.setHand(player2, new ArrayList<>(List.of(new ShuFootSoldiers())));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when Zhang Liao deals damage to its controller")
    void noTriggerWhenDamagingController() {
        Permanent zhangLiao = addCreatureReady(player1, new ZhangLiaoHeroOfHefei());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(zhangLiao.getId());
        harness.setHand(player1, new ArrayList<>(List.of(new ShuFootSoldiers())));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addAttackingZhangLiao(Player player) {
        Permanent zhangLiao = addCreatureReady(player, new ZhangLiaoHeroOfHefei());
        zhangLiao.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }
}
