package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusionistsGambit.class, GrizzlyBears.class})
class IllusionistsGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Removes and untaps attackers, then creates a restricted additional combat")
    void removesAttackersAndCreatesAdditionalCombat() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        gd.combatPhasesThisTurn = 1;
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IllusionistsGambit()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        PendingInteraction.AttackerDeclaration prompt = gd.interaction.activeInteraction(
                PendingInteraction.AttackerDeclaration.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.attackerIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Cannot be cast during the declare blockers step of your own turn")
    void cannotCastOnOwnTurn() {
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new IllusionistsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
