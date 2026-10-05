package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AutonomousAssembler;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasFoundry.class, AutonomousAssembler.class})
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
        assertThat(gqs.isLand(gd, foundry)).isTrue();
    }

    @Test
    void animationWearsOffAtEndOfTurn() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new MishrasFoundry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, foundry)).isFalse();
        assertThat(gqs.isArtifact(foundry)).isFalse();
        assertThat(foundry.getTransientSubtypes()).doesNotContain(CardSubtype.ASSEMBLY_WORKER);
    }

    @Test
    void pumpsTargetAttackingAssemblyWorker() {
        harness.addToBattlefield(player1, new MishrasFoundry());
        Permanent assembler = addCreatureReady(player1, new AutonomousAssembler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackers(List.of(1));

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

    @Test
    void canPumpAnOpponentsAttackingAssemblyWorkerAndPaysTapAndManaCosts() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new MishrasFoundry());
        Permanent assembler = addCreatureReady(player2, new AutonomousAssembler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackers(player2, List.of(0));

        harness.activateAbility(player1, 0, 1, null, assembler.getId());
        assertThat(foundry.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, assembler)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, assembler)).isEqualTo(7);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, assembler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, assembler)).isEqualTo(5);
    }

    @Test
    void doesNotPumpTargetThatStopsAttackingBeforeResolution() {
        harness.addToBattlefield(player1, new MishrasFoundry());
        Permanent assembler = addCreatureReady(player1, new AutonomousAssembler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackers(List.of(1));

        harness.activateAbility(player1, 0, 1, null, assembler.getId());
        assembler.setAttacking(false);
        assembler.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, assembler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, assembler)).isEqualTo(5);
    }

    @Test
    void tappedFoundryCanAnimateWithoutUntapping() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new MishrasFoundry());
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(foundry.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, foundry)).isTrue();
        assertThat(gqs.getEffectivePower(gd, foundry)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void animationKeepsTheManaAbility() {
        Permanent foundry = addCreatureReady(player1, new MishrasFoundry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.tapPermanent(player1, 0);

        assertThat(foundry.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void canPumpAnotherAttackingFoundryAndReanimationKeepsTheBoost() {
        harness.addToBattlefield(player1, new MishrasFoundry());
        Permanent attacker = addCreatureReady(player1, new MishrasFoundry());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(1));

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void animatedSummoningSickFoundryCannotPayTapCosts() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new MishrasFoundry());
        foundry.setSummoningSick(true);
        Permanent assembler = addCreatureReady(player1, new AutonomousAssembler());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(1));

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, assembler.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(foundry.isTapped()).isFalse();
    }
}
