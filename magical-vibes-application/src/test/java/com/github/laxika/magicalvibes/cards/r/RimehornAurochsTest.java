package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Aurochs;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RimehornAurochsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with other Aurochs gives +1/+0 for each one")
    void boostsForEachOtherAttackingAurochs() {
        Permanent rimehorn = addCreatureReady(player1, new RimehornAurochs());
        addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(rimehorn.getPowerModifier()).isEqualTo(2);
        assertThat(rimehorn.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Snow ability makes the first target block the second target if able")
    void makesTargetCreatureBlockTargetCreature() {
        addCreatureReady(player1, new RimehornAurochs());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(attacker.getId());

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }
}
