package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StirThePride.class, MyrMoonvessel.class})
class StirThePrideTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode boosts creatures you control until end of turn")
    void boostsOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new MyrMoonvessel());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new MyrMoonvessel());

        castSingleMode(0);

        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(3);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(1);
        assertThat(mine.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The second mode grants creatures you control a damage life-gain trigger")
    void grantsLifeGainTrigger() {
        Permanent attacker = addCreatureReady(player1, new MyrMoonvessel());
        attacker.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSingleMode(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Entwine resolves both modes and charges the additional mana")
    void entwineResolvesBothModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MyrMoonvessel());
        harness.setHand(player1, List.of(new StirThePride()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE)).isNotEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The life-gain trigger does not affect creatures entering after the spell resolves")
    void lifeGainTriggerDoesNotAffectLaterEntrants() {
        Permanent early = addCreatureReady(player1, new MyrMoonvessel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSingleMode(1);

        Permanent late = addCreatureReady(player1, new MyrMoonvessel());
        early.setAttacking(true);
        late.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("The life-gain trigger wears off at end of turn")
    void lifeGainTriggerWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());

        castSingleMode(1);

        assertThat(creature.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE)).isNotEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE)).isEmpty();
    }

    @Test
    @DisplayName("Entwine requires its additional mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new StirThePride()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSingleMode(int mode) {
        harness.setHand(player1, List.of(new StirThePride()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{mode}, List.of());
        harness.passBothPriorities();
    }
}
