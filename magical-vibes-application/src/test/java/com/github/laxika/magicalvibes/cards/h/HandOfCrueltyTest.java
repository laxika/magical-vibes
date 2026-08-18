package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class HandOfCrueltyTest extends BaseCardTest {

    @Test
    @DisplayName("A white creature cannot block Hand of Cruelty")
    void whiteCreatureCannotBlock() {
        Permanent hand = addHandReady(player1);
        Permanent blocker = addCreatureReady(player2, new BishopsSoldier());
        hand.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(hand)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Blocking gives Hand of Cruelty +1/+1 until end of turn")
    void blockingTriggersBushido() {
        Permanent hand = addHandReady(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(hand),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked gives Hand of Cruelty +1/+1 until end of turn")
    void becomingBlockedTriggersBushido() {
        Permanent hand = addHandReady(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        hand.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(hand))));
        harness.passBothPriorities();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonwhite creature can block Hand of Cruelty")
    void nonwhiteCreatureCanBlock() {
        Permanent hand = addHandReady(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        hand.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(hand))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addHandReady(Player player) {
        return addCreatureReady(player, new HandOfCruelty());
    }
}
