package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.ItOfTheHorridSwarm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrossroadsConsecrator.class, ItOfTheHorridSwarm.class})
class CrossroadsConsecratorTest extends BaseCardTest {

    @Test
    @DisplayName("Gives an attacking Human +1/+1 until end of turn")
    void boostsAttackingHuman() {
        Permanent attacker = setupAttackingTarget(new CrossroadsConsecrator());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = setupAttackingTarget(new CrossroadsConsecrator());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a non-Human attacking target")
    void rejectsNonHumanAttacker() {
        Permanent nonHumanAttacker = setupAttackingTarget(new ItOfTheHorridSwarm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonHumanAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a nonattacking Human target")
    void rejectsNonattackingHuman() {
        addCreatureReady(player1, new CrossroadsConsecrator());
        Permanent nonAttackingHuman = addCreatureReady(player1, new CrossroadsConsecrator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonAttackingHuman.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking Human that stops attacking is illegal on resolution")
    void targetMustStillBeAttackingOnResolution() {
        Permanent attacker = setupAttackingTarget(new CrossroadsConsecrator());
        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can boost an opponent's attacking Human")
    void boostsOpponentsAttackingHuman() {
        addCreatureReady(player1, new CrossroadsConsecrator());
        Permanent attacker = addCreatureReady(player2, new CrossroadsConsecrator());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target itself while attacking if untapped")
    void canBoostItselfWhileAttacking() {
        Permanent source = addCreatureReady(player1, new CrossroadsConsecrator());
        source.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void rejectsTappedSource() {
        Permanent attacker = setupAttackingTarget(new CrossroadsConsecrator());
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the tap cost with a summoning-sick source")
    void rejectsSummoningSickSource() {
        Permanent attacker = setupAttackingTarget(new CrossroadsConsecrator());
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires green mana to activate")
    void rejectsWrongColorMana() {
        Permanent attacker = setupAttackingTarget(new CrossroadsConsecrator());
        gd.playerManaPools.get(player1.getId()).drainNonPersistent();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setupAttackingTarget(Card targetCard) {
        addCreatureReady(player1, new CrossroadsConsecrator());
        Permanent attacker = addCreatureReady(player1, targetCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        return attacker;
    }
}
