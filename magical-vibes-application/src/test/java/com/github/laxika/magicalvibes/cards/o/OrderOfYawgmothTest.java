package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.b.BogRaiders;
import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfYawgmoth.class, ArgothianSwine.class, BogRaiders.class, Cathodion.class,
        Forest.class, SoulsFire.class})
class OrderOfYawgmothTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card of their choice")
    void combatDamageMakesDamagedPlayerDiscard() {
        addAttackingOrder(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new ArgothianSwine(), new Forest())));

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
    @DisplayName("No trigger when the Order is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingOrder(player1);
        Permanent blocker = addCreatureReady(player2, new ArgothianSwine());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("No discard choice when the damaged player has no cards")
    void noDiscardChoiceForEmptyHand() {
        addAttackingOrder(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Noncombat damage to a player makes that player discard a card")
    void noncombatDamageMakesDamagedPlayerDiscard() {
        Permanent order = addCreatureReady(player1, new OrderOfYawgmoth());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(order.getId(), player2.getId()));
        resolveAllTriggers();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void fearPreventsNonblackNonartifactBlocker() {
        Permanent blocker = addCreatureReady(player2, new ArgothianSwine());
        Permanent attacker = addAttackingOrder(player1);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void fearAllowsBlackCreatureToBlock() {
        Permanent blocker = addCreatureReady(player2, new BogRaiders());
        Permanent attacker = addAttackingOrder(player1);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void fearAllowsArtifactCreatureToBlock() {
        Permanent blocker = addCreatureReady(player2, new Cathodion());
        Permanent attacker = addAttackingOrder(player1);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingOrder(Player player) {
        Permanent order = addCreatureReady(player, new OrderOfYawgmoth());
        order.setAttacking(true);
        return order;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
