package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RimescaleDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and puts an ice counter on it")
    void tapsTargetCreatureAndPutsIceCounterOnIt() {
        addDragon(player1);
        Permanent target = addCreature(player2);
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with an ice counter does not untap during its controller's untap step")
    void creatureWithIceCounterDoesNotUntap() {
        addDragon(player1);
        Permanent target = addCreature(player2);
        target.tap();
        target.setCounterCount(CounterType.ICE, 1);

        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addDragon(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        payAbilityCost(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addDragon(player1);
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addDragon(Player player) {
        Permanent dragon = new Permanent(new RimescaleDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(dragon);
        return dragon;
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void payAbilityCost(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        gd.playerManaPools.get(player.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
