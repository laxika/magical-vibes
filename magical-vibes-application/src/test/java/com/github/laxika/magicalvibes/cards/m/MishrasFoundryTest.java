package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AutonomousAssembler;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MishrasFoundryTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new MishrasFoundry());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void animationMakesItAnAssemblyWorkerArtifactCreatureAndKeepsItALand() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new MishrasFoundry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, foundry)).isTrue();
        assertThat(gqs.isArtifact(foundry)).isTrue();
        assertThat(gqs.getEffectivePower(gd, foundry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, foundry)).isEqualTo(2);
        assertThat(foundry.getTransientSubtypes()).contains(CardSubtype.ASSEMBLY_WORKER);
        assertThat(foundry.getCard().getType()).isEqualTo(CardType.LAND);
    }

    @Test
    void animationWearsOffAtEndOfTurn() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new MishrasFoundry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, foundry)).isFalse();
        assertThat(gqs.isArtifact(foundry)).isFalse();
        assertThat(foundry.getTransientSubtypes()).doesNotContain(CardSubtype.ASSEMBLY_WORKER);
    }

    @Test
    void pumpsTargetAttackingAssemblyWorker() {
        harness.addToBattlefield(player1, new MishrasFoundry());
        Permanent assembler = addCreatureReady(player1, new AutonomousAssembler());
        assembler.setAttacking(true);
        assembler.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, assembler.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, assembler)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, assembler)).isEqualTo(7);
    }

    @Test
    void cannotPumpNonAttackingAssemblyWorker() {
        harness.addToBattlefield(player1, new MishrasFoundry());
        Permanent assembler = addCreatureReady(player1, new AutonomousAssembler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, assembler.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
