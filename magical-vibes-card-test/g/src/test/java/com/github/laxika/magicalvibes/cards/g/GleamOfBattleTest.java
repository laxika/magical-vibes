package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosMastiff;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GleamOfBattle.class, BorosMastiff.class})
class GleamOfBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacking creature you control gets a +1/+1 counter")
    void eachAttackerGetsACounter() {
        addGleamOfBattle(player1);
        Permanent attacker1 = addCreature(player1);
        Permanent attacker2 = addCreature(player1);
        Permanent idle = addCreature(player1);

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(attacker1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(idle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker1.getEffectivePower()).isEqualTo(3);
        assertThat(attacker1.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures an opponent controls don't trigger it")
    void opponentAttackersGetNoCounter() {
        addGleamOfBattle(player1);
        Permanent opponentAttacker = addCreature(player2);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(opponentAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two Gleams each put a counter on the attacking creature")
    void multipleGleamsEachTrigger() {
        addGleamOfBattle(player1);
        addGleamOfBattle(player1);
        Permanent attacker = addCreature(player1);

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing Gleam after it triggers does not stop the counter")
    void triggerResolvesAfterEnchantmentLeaves() {
        Permanent gleam = harness.addToBattlefieldAndReturn(player1, new GleamOfBattle());
        Permanent attacker = addCreature(player1);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(gleam);
        gd.playerGraveyards.get(player1.getId()).add(gleam.getCard());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature returning to the battlefield is not the original attacker")
    void returnedCreatureDoesNotReceiveOldTriggerCounter() {
        addGleamOfBattle(player1);
        Permanent attacker = addCreature(player1);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, attacker.getCard());
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addGleamOfBattle(Player player) {
        harness.addToBattlefield(player, new GleamOfBattle());
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new BorosMastiff());
        creature.setSummoningSick(false);
        return creature;
    }
}
