package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.k.KnowledgeVault;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifeMatrix.class, PsionicEntity.class, KnowledgeVault.class})
class LifeMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Life Matrix puts a matrix counter on the target creature")
    void putsMatrixCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player2, new PsionicEntity());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player2, "Psionic Entity");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent matrix = findPermanent(player1, "Life Matrix");
        Permanent creature = findPermanent(player2, "Psionic Entity");
        assertThat(matrix.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(creature.getCounterCount(CounterType.MATRIX)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with a matrix counter can use the granted regeneration ability")
    void grantedAbilityRegeneratesCreature() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player1, new PsionicEntity());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player1, "Psionic Entity");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Psionic Entity");
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MATRIX)).isZero();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature can use the granted regeneration ability")
    void opponentCreatureCanUseGrantedAbility() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player2, new PsionicEntity());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player2, "Psionic Entity");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player2, "Psionic Entity");
        assertThat(creature.getCounterCount(CounterType.MATRIX)).isZero();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted regeneration shield saves the creature from lethal damage")
    void grantedRegenerationShieldSavesCreature() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player1, new PsionicEntity());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player1, "Psionic Entity");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Psionic Entity");
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Life Matrix cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player2, new KnowledgeVault());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID artifactId = harness.getPermanentId(player2, "Knowledge Vault");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Life Matrix can only be activated during its controller's upkeep")
    void activationRequiresUpkeep() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player1, new PsionicEntity());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID creatureId = harness.getPermanentId(player1, "Psionic Entity");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }
}
