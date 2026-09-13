package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneArmor.class, FlowstoneCrusher.class})
class FlowstoneArmorTest extends BaseCardTest {

    @Test
    void abilityGivesTargetCreaturePlusOneMinusOneWhileArtifactIsTapped() {
        addReadyArmor(player1);
        Permanent crusher = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, crusher.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(3);
    }

    @Test
    void abilityBoostPersistsPastEndOfTurnWhileArtifactStaysTapped() {
        addReadyArmor(player1);
        Permanent crusher = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, crusher.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(3);
    }

    @Test
    void abilityBoostEndsWhenArtifactUntaps() {
        Permanent armor = addReadyArmor(player1);
        Permanent crusher = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, crusher.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(armor.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(4);
    }

    @Test
    void abilityBoostPersistsWhenControllerChoosesNotToUntapArtifact() {
        Permanent armor = addReadyArmor(player1);
        Permanent crusher = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, crusher.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(armor.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(3);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        addReadyArmor(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FlowstoneArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyArmor(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FlowstoneArmor());
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceActivePlayer(currentActivePlayer);
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
