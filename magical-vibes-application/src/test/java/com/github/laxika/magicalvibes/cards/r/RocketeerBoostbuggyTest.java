package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CanyonVaulter;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rocketeer Boostbuggy")
@CardUsed({RocketeerBoostbuggy.class, CanyonVaulter.class, EnsoulArtifact.class})
class RocketeerBoostbuggyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when it attacks")
    void createsTreasureWhenItAttacks() {
        addCreatureReady(player1, new RocketeerBoostbuggy());
        addCreatureReady(player1, new CanyonVaulter());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    @DisplayName("Exhaust makes it a permanent artifact creature and puts a counter on it")
    void exhaustMakesItPermanentArtifactCreatureAndPutsCounterOnIt() {
        Permanent buggy = addCreatureReady(player1, new RocketeerBoostbuggy());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, buggy)).isTrue();
        assertThat(gqs.isArtifact(buggy)).isTrue();
        assertThat(buggy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, buggy)).isTrue();
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new RocketeerBoostbuggy());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust preserves an existing effect setting base power and toughness")
    void exhaustPreservesExistingBasePowerAndToughness() {
        Permanent buggy = addCreatureReady(player1, new RocketeerBoostbuggy());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, buggy.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, buggy)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, buggy)).isEqualTo(5);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(buggy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, buggy)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, buggy)).isEqualTo(6);
    }

    @Test
    @DisplayName("Crew taps the creature and expires after the turn")
    void crewTapsCreatureAndExpiresAfterTurn() {
        Permanent buggy = addCreatureReady(player1, new RocketeerBoostbuggy());
        Permanent pilot = addCreatureReady(player1, new CanyonVaulter());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, buggy)).isTrue();
        assertThat(buggy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, buggy)).isFalse();
        assertThat(gqs.isArtifact(buggy)).isTrue();
    }

    @Test
    @DisplayName("Crew requires an untapped creature with enough power")
    void crewCannotBeActivatedWithoutCreature() {
        addCreatureReady(player1, new RocketeerBoostbuggy());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exhaust persists through cleanup and permits attacking without crew")
    void exhaustPersistsThroughCleanupAndAllowsAttackingWithoutCrew() {
        Permanent buggy = addCreatureReady(player1, new RocketeerBoostbuggy());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, buggy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, buggy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, buggy)).isEqualTo(3);

        advanceToUpkeep(player1);
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }
}
