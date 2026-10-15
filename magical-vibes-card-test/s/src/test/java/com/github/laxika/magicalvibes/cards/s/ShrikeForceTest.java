package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrikeForce.class, BakersbaneDuo.class})
class ShrikeForceTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Shrike Force")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new ShrikeForce());
        addCreatureReady(player2, new BakersbaneDuo());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Vigilance and double strike work in combat")
    void vigilanceAndDoubleStrikeWorkInCombat() {
        Permanent shrikeForce = addCreatureReady(player1, new ShrikeForce());

        declareAttackers(player1, List.of(0));
        assertThat(shrikeForce.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A flying blocker takes both strikes without damage reaching the defending player")
    void flyingCreatureCanBlockBothStrikes() {
        Permanent attacker = addCreatureReady(player1, new ShrikeForce());
        Permanent blocker = addCreatureReady(player2, new ShrikeForce());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Shrike Force deals damage in both steps when blocking a ground creature")
    void doubleStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new BakersbaneDuo());
        Permanent blocker = addCreatureReady(player2, new ShrikeForce());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
