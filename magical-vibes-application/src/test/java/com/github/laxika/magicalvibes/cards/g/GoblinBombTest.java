package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBomb.class})
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
    @DisplayName("Accepting the upkeep flip with no fuse counters never creates a negative count")
    void upkeepFlipAtZeroCountersNeverGoesNegative() {
        Permanent bomb = addBomb(player1);

        flipAtUpkeep(true);

        assertThat(bomb.getCounterCount(CounterType.FUSE)).isIn(0, 1);
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
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void upkeepAbilityDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent bomb = addBomb(player1);
        bomb.setCounterCount(CounterType.FUSE, 3);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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
        assertThat(bomb.getCounterCount(CounterType.FUSE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Bomb"));
    }

    @CardUsed({ChandraNalaar.class})
    @Test
    @DisplayName("Removing five fuse counters and sacrificing deals 20 damage to a target planeswalker")
    void abilityDeals20DamageToPlaneswalker() {
        Permanent bomb = addBomb(player1);
        bomb.setCounterCount(CounterType.FUSE, 5);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 25);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(bomb.getCounterCount(CounterType.FUSE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
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
        assertThat(bomb.getCounterCount(CounterType.FUSE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bomb);
    }

    @Test
    @DisplayName("Cannot target a non-planeswalker permanent")
    void cannotTargetNonPlaneswalkerPermanent() {
        Permanent bomb = addBomb(player1);
        bomb.setCounterCount(CounterType.FUSE, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bomb.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bomb.getCounterCount(CounterType.FUSE)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bomb);
    }

    private void flipAtUpkeep(boolean accept) {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private Permanent addBomb(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new GoblinBomb());
    }
}
