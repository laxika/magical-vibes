package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NumaiOutcast.class, HumbleBudoka.class})
class NumaiOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 2 triggers when Numai Outcast becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent outcast = addCreatureReady(player1, new NumaiOutcast());
        addCreatureReady(player2, new HumbleBudoka());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(outcast.getPowerModifier()).isEqualTo(2);
        assertThat(outcast.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido 2 triggers when Numai Outcast blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new HumbleBudoka());
        Permanent outcast = addCreatureReady(player2, new NumaiOutcast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(outcast.getPowerModifier()).isEqualTo(2);
        assertThat(outcast.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The bushido bonus wears off at end of turn")
    void bushidoWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new HumbleBudoka());
        Permanent outcast = addCreatureReady(player2, new NumaiOutcast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        assertThat(outcast.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(outcast.getPowerModifier()).isEqualTo(0);
        assertThat(outcast.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An unblocked Numai Outcast gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent outcast = addCreatureReady(player1, new NumaiOutcast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(outcast.getPowerModifier()).isZero();
        assertThat(outcast.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A Numai Outcast blocked by multiple creatures gets only one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent outcast = addCreatureReady(player1, new NumaiOutcast());
        addCreatureReady(player2, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(outcast.getPowerModifier()).isEqualTo(2);
        assertThat(outcast.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Numai Outcast blocking multiple creatures gets only one Bushido bonus")
    void blocksMultipleCreaturesGetsOneBushidoBonus() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player1, new HumbleBudoka());
        NumaiOutcast card = new NumaiOutcast();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent outcast = addCreatureReady(player2, card);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(outcast.getPowerModifier()).isEqualTo(2);
        assertThat(outcast.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {B} and 5 life grants a regeneration shield")
    void payManaAndLifeGrantsRegenerationShield() {
        Permanent outcast = addCreatureReady(player1, new NumaiOutcast());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(outcast.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability with less than 5 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new NumaiOutcast());
        harness.setLife(player1, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("The regeneration shield saves Numai Outcast from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent outcast = addCreatureReady(player1, new NumaiOutcast());
        outcast.setRegenerationShield(1);
        outcast.setBlocking(true);
        outcast.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new HumbleBudoka());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Numai Outcast");
        assertThat(outcast.isTapped()).isTrue();
        assertThat(outcast.getRegenerationShield()).isEqualTo(0);
    }
}
