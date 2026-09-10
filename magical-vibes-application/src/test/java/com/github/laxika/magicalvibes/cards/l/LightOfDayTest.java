package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.Souldrinker;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightOfDay.class, Souldrinker.class, TrainedArmodon.class})
class LightOfDayTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature cannot attack while Light of Day is on the battlefield")
    void blackCreatureCannotAttack() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent black = addCreatureReady(player1, new Souldrinker());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(black);
        assertThatThrownBy(() -> declareAttackers(List.of(idx)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black creature attacks normally while Light of Day is on the battlefield")
    void nonBlackCreatureCanAttack() {
        harness.addToBattlefield(player1, new LightOfDay());
        harness.setLife(player2, 20);
        Permanent nonblack = addCreatureReady(player1, new TrainedArmodon());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(nonblack);
        declareAttackers(List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Black creature cannot block while Light of Day is on the battlefield")
    void blackCreatureCannotBlock() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent attacker = addCreatureReady(player1, new TrainedArmodon());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Souldrinker());

        prepareDeclareBlockers();

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black creature blocks normally while Light of Day is on the battlefield")
    void nonBlackCreatureCanBlock() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent attacker = addCreatureReady(player1, new TrainedArmodon());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TrainedArmodon());

        prepareDeclareBlockers();
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIdx)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Black creature can attack again after Light of Day leaves the battlefield")
    void restrictionLiftsWhenLightOfDayLeaves() {
        Permanent lightOfDay = harness.addToBattlefieldAndReturn(player1, new LightOfDay());
        harness.setLife(player2, 20);
        Permanent black = addCreatureReady(player1, new Souldrinker());

        gd.playerBattlefields.get(player1.getId()).remove(lightOfDay);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(black);
        declareAttackers(List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
