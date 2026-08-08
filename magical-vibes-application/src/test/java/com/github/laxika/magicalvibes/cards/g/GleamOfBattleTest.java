package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GleamOfBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacking creature you control gets a +1/+1 counter")
    void eachAttackerGetsACounter() {
        addGleamOfBattle(player1);
        Permanent attacker1 = addCreature(player1);
        Permanent attacker2 = addCreature(player1);
        Permanent idle = addCreature(player1);

        declareAttackers(List.of(1, 2));
        resolveQueuedTriggers();

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
        resolveQueuedTriggers();

        assertThat(opponentAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    /** Resolve every triggered ability currently on the stack (one Gleam of Battle trigger per attacker). */
    private void resolveQueuedTriggers() {
        int triggers = gd.stack.size();
        for (int i = 0; i < triggers; i++) {
            harness.passBothPriorities();
        }
    }

    private void addGleamOfBattle(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new GleamOfBattle()));
    }

    private Permanent addCreature(Player player) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{R}");
        creature.setColor(CardColor.RED);
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
