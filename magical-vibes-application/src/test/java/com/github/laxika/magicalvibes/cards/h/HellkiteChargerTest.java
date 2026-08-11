package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HellkiteChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the attack trigger untaps attackers and grants an additional combat phase")
    void payingUntapsAttackersAndGrantsExtraCombat() {
        Permanent charger = addCreatureReady(player1, new HellkiteCharger());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        nonAttacker.tap();
        harness.addMana(player1, ManaColor.RED, 7);

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(charger.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(charger.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(nonAttacker.isTapped()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves creatures tapped and grants no additional combat phase")
    void decliningLeavesAttackersTapped() {
        Permanent charger = addCreatureReady(player1, new HellkiteCharger());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(charger.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
