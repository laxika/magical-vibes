package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BubblingBeeblesTest extends BaseCardTest {

    @Test
    @DisplayName("Bubbling Beebles can't be blocked when defending player controls an enchantment")
    void cantBeBlockedWhenDefenderControlsEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        Permanent beebles = attackingBeebles();

        beginBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(beebles)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Bubbling Beebles can be blocked when defending player controls no enchantments")
    void canBeBlockedWhenDefenderControlsNoEnchantment() {
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        Permanent beebles = attackingBeebles();

        beginBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(beebles))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Bubbling Beebles deals 3 damage")
    void dealsDamageWhenUnblocked() {
        harness.setLife(player2, 20);
        attackingBeebles();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent attackingBeebles() {
        Permanent beebles = new Permanent(new BubblingBeebles());
        beebles.setSummoningSick(false);
        beebles.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(beebles);
        return beebles;
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
