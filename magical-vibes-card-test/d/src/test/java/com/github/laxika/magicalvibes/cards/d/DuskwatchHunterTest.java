package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DuskwatchHunter.class)
class DuskwatchHunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskwatchHunter());
        harness.setHand(player1, List.of(new DuskwatchHunter()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be blocked by a token")
    void cannotBeBlockedByToken() {
        Permanent attacker = addReadyAttacker();
        Permanent token = addToken(player2);

        prepareDeclareBlockers(player1);

        int tokenIndex = gd.playerBattlefields.get(player2.getId()).indexOf(token);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(tokenIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a nontoken creature")
    void canBeBlockedByNontokenCreature() {
        Permanent attacker = addReadyAttacker();
        Permanent blocker = addCreatureReady(player2, new DuskwatchHunter());

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("ETB can target the entering Hunter itself")
    void etbCanTargetItself() {
        Permanent hunter = harness.enterBattlefieldAndReturn(player1, new DuskwatchHunter());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, hunter.getId());
        }
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not move its counter to another creature when its target leaves")
    void etbDoesNotRetargetAfterTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskwatchHunter());
        harness.setHand(player1, List.of(new DuskwatchHunter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent hunter = findPermanent(player1, "Duskwatch Hunter");
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyAttacker() {
        Permanent attacker = addCreatureReady(player1, new DuskwatchHunter());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addToken(Player player) {
        DuskwatchHunter card = new DuskwatchHunter();
        card.setToken(true);
        return addCreatureReady(player, card);
    }
}
