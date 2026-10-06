package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
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

@CardUsed({Scorchwalker.class, DiscipleOfTheOldWays.class})
class ScorchwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodrush gives target attacking creature +5/+1 until end of turn")
    void bloodrushBoostsAttackingCreature() {
        harness.setHand(player1, List.of(new Scorchwalker()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Scorchwalker");
    }

    @Test
    @DisplayName("The bloodrush boost wears off at end of turn")
    void bloodrushBoostWearsOff() {
        harness.setHand(player1, List.of(new Scorchwalker()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodrush cannot target a creature that isn't attacking")
    void bloodrushRejectsNonAttackingCreature() {
        harness.setHand(player1, List.of(new Scorchwalker()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Scorchwalker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodrush pays generic mana with another color and discards before resolution")
    void bloodrushPaysCostsBeforeResolving() {
        harness.setHand(player1, List.of(new Scorchwalker()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());

        harness.assertNotInHand(player1, "Scorchwalker");
        harness.assertInGraveyard(player1, "Scorchwalker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodrush can target an opponent's attacking creature")
    void bloodrushCanBoostOpposingAttacker() {
        harness.setHand(player1, List.of(new Scorchwalker()));
        Permanent attacker = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Scorchwalker");
    }

    @Test
    @DisplayName("Bloodrush does not boost a target that stops attacking before resolution")
    void bloodrushRechecksAttackingRestriction() {
        harness.setHand(player1, List.of(new Scorchwalker()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateHandAbility(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Scorchwalker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodrush requires two red mana even when three mana are available")
    void bloodrushRejectsInsufficientRedMana() {
        harness.setHand(player1, List.of(new Scorchwalker()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Scorchwalker");
        harness.assertNotInGraveyard(player1, "Scorchwalker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
