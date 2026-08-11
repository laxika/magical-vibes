package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StirThePrideTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode boosts creatures you control until end of turn")
    void boostsOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSingleMode(0);

        assertThat(mine.getEffectivePower()).isEqualTo(4);
        assertThat(mine.getEffectiveToughness()).isEqualTo(4);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The second mode grants creatures you control a damage life-gain trigger")
    void grantsLifeGainTrigger() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSingleMode(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Entwine resolves both modes and charges the additional mana")
    void entwineResolvesBothModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StirThePride()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE)).isNotEmpty();
    }

    private void castSingleMode(int mode) {
        harness.setHand(player1, List.of(new StirThePride()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{mode}, List.of());
        harness.passBothPriorities();
    }
}
