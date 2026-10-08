package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SmashingSpree.class, GrizzlyBears.class})
class SmashingSpreeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target attacking creature +3/+3 and trample")
    void boostsAttackingCreatureAndGrantsTrample() {
        Permanent attacker = addAttacker();

        castResolve(attacker);

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(5);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addAttacker();

        castResolve(attacker);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new SmashingSpree()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    @Test
    @DisplayName("Can boost an opponent's attacking creature")
    void canTargetOpponentsAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        castResolve(attacker);

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(5);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not resolve if the target stops attacking before resolution")
    void targetMustStillBeAttackingAtResolution() {
        Permanent attacker = addAttacker();
        harness.setHand(player1, List.of(new SmashingSpree()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Smashing Spree");
    }

    @Test
    @DisplayName("Resolved effects persist after the creature stops attacking")
    void effectsPersistAfterCombat() {
        Permanent attacker = addAttacker();
        castResolve(attacker);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        attacker.setAttacking(false);

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(5);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        return attacker;
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new SmashingSpree()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
