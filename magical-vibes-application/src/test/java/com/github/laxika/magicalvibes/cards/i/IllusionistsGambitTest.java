package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusionistsGambit.class, BalefulStrix.class})
class IllusionistsGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Removes and untaps attackers, then creates a restricted additional combat")
    void removesAttackersAndCreatesAdditionalCombat() {
        Permanent attacker = addCreatureReady(player1, new BalefulStrix());
        addCreatureReady(player1, new BalefulStrix());
        addCreatureReady(player2, new BalefulStrix());

        declareAttackers(List.of(0));
        gd.combatPhasesThisTurn = 1;
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IllusionistsGambit()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player2, 0);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

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
        addCreatureReady(player1, new BalefulStrix());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new IllusionistsGambit()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {
            "PRECOMBAT_MAIN", "BEGINNING_OF_COMBAT", "DECLARE_ATTACKERS",
            "COMBAT_DAMAGE", "END_OF_COMBAT", "POSTCOMBAT_MAIN"
    })
    void cannotCastOutsideOpponentsDeclareBlockers(TurnStep step) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IllusionistsGambit()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void additionalCombatRestrictionsDoNotUseTheStack() {
        addCreatureReady(player1, new BalefulStrix());
        addCreatureReady(player2, new BalefulStrix());
        declareAttackers(List.of(0));
        gd.combatPhasesThisTurn = 1;
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IllusionistsGambit()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player2, 0);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mostRecentlyCreatedCombatOccursFirst() {
        Permanent attacker = addCreatureReady(player1, new BalefulStrix());
        addCreatureReady(player2, new BalefulStrix());
        declareAttackers(List.of(0));
        gd.combatPhasesThisTurn = 1;
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IllusionistsGambit(), new IllusionistsGambit()));
        harness.addMana(player2, ManaColor.BLUE, 8);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.castAndResolveInstant(player2, 0));
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        harness.castInstant(player2, 0);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        PendingInteraction.AttackerDeclaration prompt = gd.interaction.activeInteraction(
                PendingInteraction.AttackerDeclaration.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.attackerIndices()).containsExactly(0);
    }

    @Test
    void removesEveryAttackerWithoutUntappingNonattackers() {
        Permanent first = addCreatureReady(player1, new BalefulStrix());
        Permanent second = addCreatureReady(player1, new BalefulStrix());
        Permanent nonattacker = addCreatureReady(player1, new BalefulStrix());
        nonattacker.tap();
        addCreatureReady(player2, new BalefulStrix());
        declareAttackers(List.of(0, 1));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IllusionistsGambit()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.castAndResolveInstant(player2, 0));

        assertThat(first.isAttacking()).isFalse();
        assertThat(second.isAttacking()).isFalse();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(nonattacker.isTapped()).isTrue();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.assertLife(player2, 20);
    }
}
