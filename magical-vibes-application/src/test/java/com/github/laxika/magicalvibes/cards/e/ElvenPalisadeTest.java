package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvenPalisadeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest weakens the targeted attacking creature")
    void sacrificesForestToWeakenAttacker() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addAttacker();
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, attacker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(-3);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.getEffectivePower()).isEqualTo(0);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Only attacking creatures can be targeted")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        harness.addToBattlefield(player1, new Forest());
        Permanent bystander = addCreatureReady(player1, new HillGiant());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    @Test
    @DisplayName("A Forest is required for the activation cost")
    void requiresForestToSacrifice() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        harness.addToBattlefield(player1, new Plains());
        Permanent attacker = addAttacker();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setAttacking(true);
        return attacker;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
