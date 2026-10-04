package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthKingdomSoldier.class})
class EarthKingdomSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of two target creatures you control")
    void putsCountersOnTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EarthKingdomSoldier());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EarthKingdomSoldier());

        castSoldier(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on one target creature you control")
    void putsCounterOnOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EarthKingdomSoldier());

        castSoldier(List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast without targets")
    void canBeCastWithoutTargets() {
        castSoldier(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Earth Kingdom Soldier")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Earth Kingdom Soldier");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new EarthKingdomSoldier());
        castUntilEntry();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can target itself after entering the battlefield")
    void canTargetItself() {
        castUntilEntry();
        Permanent soldier = findPermanent(player1, "Earth Kingdom Soldier");

        chooseTargets(List.of(soldier.getId()));
        resolveAllTriggers();

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EarthKingdomSoldier());
        castUntilEntry();
        harness.handlePermanentChosen(player1, target.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still puts a counter on the surviving target when the other target leaves")
    void resolvesForRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EarthKingdomSoldier());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EarthKingdomSoldier());
        castUntilEntry();
        chooseTargets(List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getOriginalCard());

        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castSoldier(List<UUID> targetIds) {
        castUntilEntry();
        chooseTargets(targetIds);
        resolveAllTriggers();
    }

    private void castUntilEntry() {
        harness.setHand(player1, List.of(new EarthKingdomSoldier()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseTargets(List<UUID> targetIds) {
        for (UUID targetId : targetIds) {
            harness.handlePermanentChosen(player1, targetId);
        }
        if (targetIds.size() < 2) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
