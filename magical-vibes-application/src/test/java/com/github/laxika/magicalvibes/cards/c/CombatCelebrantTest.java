package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CombatCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addCreatureReady(player1, new CombatCelebrant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting untaps other creatures, keeps the Celebrant exerted, and grants an extra combat phase")
    void exertUntapsOthersAndGrantsExtraCombat() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(celebrant.isTapped()).isTrue();
        assertThat(celebrant.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining exert leaves other creatures tapped and grants no extra combat")
    void decliningExertDoesNothing() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(celebrant.getSkipUntapCount()).isZero();
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
