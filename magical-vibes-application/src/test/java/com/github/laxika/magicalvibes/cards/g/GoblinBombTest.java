package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinBombTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep flip either adds or removes a fuse counter")
    void upkeepFlipAddsOrRemovesFuseCounter() {
        Permanent bomb = addBomb(player1);
        bomb.setCounterCount(CounterType.FUSE, 3);

        flipAtUpkeep(true);

        assertThat(bomb.getCounterCount(CounterType.FUSE)).isIn(2, 4);
    }

    @Test
    @DisplayName("Declining the upkeep flip leaves the fuse counters untouched")
    void decliningUpkeepFlipKeepsCounters() {
        Permanent bomb = addBomb(player1);
        bomb.setCounterCount(CounterType.FUSE, 3);

        flipAtUpkeep(false);

        assertThat(bomb.getCounterCount(CounterType.FUSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing five fuse counters and sacrificing deals 20 damage to target player")
    void abilityDeals20DamageAndSacrificesSelf() {
        Permanent bomb = addBomb(player1);
        bomb.setCounterCount(CounterType.FUSE, 5);
        harness.setLife(player2, 30);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Bomb"));
    }

    @Test
    @DisplayName("Cannot activate with fewer than five fuse counters")
    void cannotActivateWithoutFiveCounters() {
        Permanent bomb = addBomb(player1);
        bomb.setCounterCount(CounterType.FUSE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void flipAtUpkeep(boolean accept) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires the trigger
        harness.passBothPriorities(); // resolve the MayEffect
        harness.handleMayAbilityChosen(player1, accept);
    }

    private Permanent addBomb(Player owner) {
        Permanent perm = new Permanent(new GoblinBomb());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }
}
