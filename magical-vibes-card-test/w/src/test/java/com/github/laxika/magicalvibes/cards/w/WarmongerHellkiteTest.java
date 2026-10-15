package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WarmongerHellkite.class, GrizzlyBears.class})
class WarmongerHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures must attack each combat if able")
    void allCreaturesMustAttackWhenAble() {
        addCreatureReady(player1, new WarmongerHellkite());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Opposing creatures must attack while Hellkite is on the battlefield")
    void opposingCreaturesMustAttack() {
        addCreatureReady(player1, new WarmongerHellkite());
        addCreatureReady(player2, new WarmongerHellkite());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Summoning-sick creatures are not required to attack")
    void summoningSickCreaturesAreNotRequiredToAttack() {
        Permanent ready = addCreatureReady(player1, new WarmongerHellkite());
        Permanent sick = harness.addToBattlefieldAndReturn(player1, new WarmongerHellkite());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(ready.isAttacking()).isTrue();
        assertThat(sick.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Repeated activations boost opposing attackers but not the defending Hellkite")
    void repeatedActivationsBoostOpposingAttackers() {
        Permanent defending = addCreatureReady(player1, new WarmongerHellkite());
        Permanent attacking = addCreatureReady(player2, new WarmongerHellkite());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        });

        assertThat(gqs.getEffectivePower(gd, attacking)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacking)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, defending)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating before combat does not boost creatures that attack later")
    void activatingBeforeCombatDoesNotBoostLaterAttackers() {
        Permanent hellkite = addCreatureReady(player1, new WarmongerHellkite());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(hellkite.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, hellkite)).isEqualTo(5);
    }

    @Test
    @DisplayName("The activated ability boosts attacking creatures until end of turn")
    void activatedAbilityBoostsAttackingCreaturesUntilEndOfTurn() {
        Permanent hellkite = addCreatureReady(player1, new WarmongerHellkite());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellkite)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellkite)).isEqualTo(5);
    }
}
