package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.Souldrinker;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightOfDay.class, ScatheZombies.class, Souldrinker.class, TrainedArmodon.class})
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
        addCreatureReady(player2, new Souldrinker());

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black creature blocks normally while Light of Day is on the battlefield")
    void nonBlackCreatureCanBlock() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent attacker = addCreatureReady(player1, new TrainedArmodon());
        addCreatureReady(player2, new TrainedArmodon());

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIdx)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
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

    @Test
    @DisplayName("A face-down Light of Day does not restrict black creatures")
    void faceDownLightOfDayHasNoEffect() {
        Permanent lightOfDay = harness.addToBattlefieldAndReturn(player1, new LightOfDay());
        lightOfDay.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setLife(player2, 20);
        Permanent black = addCreatureReady(player1, new ScatheZombies());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(black);
        declareAttackers(List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An opponent's Light of Day prevents black creatures from attacking")
    void opponentsLightOfDayPreventsAttacking() {
        harness.addToBattlefield(player2, new LightOfDay());
        Permanent black = addCreatureReady(player1, new ScatheZombies());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(black);
        assertThatThrownBy(() -> declareAttackers(List.of(idx)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature that gains black in addition to green cannot attack or block")
    void multicoloredBlackCreatureCannotAttackOrBlock() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent creature = addCreatureReady(player1, new TrainedArmodon());
        creature.getGrantedColors().add(CardColor.BLACK);

        assertThat(als.canAttack(gd, creature, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("A face-down black card is colorless and can attack and block")
    void faceDownBlackCreatureIsNotRestricted() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent creature = addCreatureReady(player1, new ScatheZombies());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();
        assertThat(bls.canBlock(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Light of Day prevents its controller's black creatures from blocking")
    void controllersBlackCreatureCannotBlock() {
        harness.addToBattlefield(player2, new LightOfDay());
        Permanent attacker = addCreatureReady(player1, new TrainedArmodon());
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Black creatures can block after Light of Day leaves the battlefield")
    void blockingRestrictionLiftsWhenLightOfDayLeaves() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LightOfDay());
        Permanent attacker = addCreatureReady(player1, new TrainedArmodon());
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));
        gd.playerBattlefields.get(player1.getId()).remove(light);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }
}
