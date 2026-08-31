package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LifeMatrix.class, GrizzlyBears.class, FountainOfYouth.class})
class LifeMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Life Matrix puts a matrix counter on the target creature")
    void putsMatrixCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        beginUpkeep(player1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(creature.getCounterCount(CounterType.MATRIX)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with a matrix counter can use the granted regeneration ability")
    void grantedAbilityRegeneratesCreature() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        beginUpkeep(player1);

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MATRIX)).isZero();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Life Matrix cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        beginUpkeep(player1);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Life Matrix can only be activated during its controller's upkeep")
    void activationRequiresUpkeep() {
        harness.addToBattlefield(player1, new LifeMatrix());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    private void beginUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }
}
