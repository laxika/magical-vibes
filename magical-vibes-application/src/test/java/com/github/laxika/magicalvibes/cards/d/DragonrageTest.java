package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
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

@CardUsed({Dragonrage.class, FeralKrushok.class})
class DragonrageTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana for each attacking creature you control")
    void addsManaPerAttackingCreature() {
        addAttacker();
        addAttacker();
        addCreatureReady(player1, new FeralKrushok());
        addCreatureReady(player2, new FeralKrushok());

        prepareCombatStep();
        castDragonrage();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking creatures gain the red-mana pump ability")
    void attackingCreaturesGainPumpAbility() {
        Permanent attacker = addAttacker();
        addAttacker();
        Permanent nonAttacker = addCreatureReady(player1, new FeralKrushok());

        prepareCombatStep();
        castDragonrage();

        assertThat(attacker.isAttacking()).isTrue();
        int basePower = gqs.getEffectivePower(gd, attacker);
        harness.activateAbility(player1, 0, null, null);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        assertThat(nonAttacker.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The granted ability wears off at end of turn")
    void grantedAbilityWearsOffAtEndOfTurn() {
        addAttacker();

        prepareCombatStep();
        castDragonrage();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @Test
    @DisplayName("With no attackers, adds no mana and grants no ability")
    void noAttackers() {
        addCreatureReady(player1, new FeralKrushok());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castDragonrage();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @Test
    @DisplayName("Creatures that start attacking after resolution do not gain the ability")
    void laterAttackerDoesNotGainAbility() {
        addAttacker();
        Permanent laterAttacker = addCreatureReady(player1, new FeralKrushok());
        prepareCombatStep();
        castDragonrage();

        laterAttacker.setAttacking(true);
        laterAttacker.setAttackTarget(player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @Test
    @DisplayName("Opponent's attackers neither produce mana nor gain the pump ability")
    void excludesOpponentsAttackers() {
        Permanent opponentAttacker = addCreatureReady(player2, new FeralKrushok());
        opponentAttacker.setAttacking(true);
        opponentAttacker.setAttackTarget(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        castDragonrage();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @Test
    @DisplayName("Granted ability survives leaving combat and repeated pumps expire at cleanup")
    void abilityPersistsAfterCombatAndPumpsExpire() {
        Permanent attacker = addAttacker();
        prepareCombatStep();
        castDragonrage();
        int basePower = gqs.getEffectivePower(gd, attacker);
        int baseToughness = gqs.getEffectiveToughness(gd, attacker);

        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(baseToughness);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Counts attackers and grants abilities at resolution rather than casting")
    void checksAttackersAtResolution() {
        Permanent removedFromCombat = addAttacker();
        Permanent remainingAttacker = addAttacker();
        prepareCombatStep();
        harness.setHand(player1, List.of(new Dragonrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);

        removedFromCombat.setAttacking(false);
        removedFromCombat.setAttackTarget(null);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        int basePower = gqs.getEffectivePower(gd, remainingAttacker);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, remainingAttacker)).isEqualTo(basePower + 1);
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new FeralKrushok());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private void prepareCombatStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
    }

    private void castDragonrage() {
        harness.setHand(player1, List.of(new Dragonrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(gd.currentStep, () -> harness.castAndResolveInstant(player1, 0));
    }
}
