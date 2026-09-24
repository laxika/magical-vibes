package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Caltrops;
import com.github.laxika.magicalvibes.cards.c.CapashenTemplar;
import com.github.laxika.magicalvibes.cards.f.FledglingOsprey;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
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

@CardUsed({ArcheryTraining.class, Caltrops.class, CapashenTemplar.class,
        FledglingOsprey.class, GoliathBeetle.class})
class ArcheryTrainingTest extends BaseCardTest {

    private Permanent addEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new FledglingOsprey());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArcheryTraining());
        aura.setAttachedTo(creature.getId());
        return creature;
    }

    // ===== Upkeep trigger: arrow counter =====

    @Test
    @DisplayName("Accepting the upkeep trigger puts an arrow counter on the Aura")
    void upkeepAcceptedAddsArrowCounter() {
        Permanent creature = addEnchantedCreature();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent auraPerm = findPermanent(player1, "Archery Training");
        assertThat(auraPerm.getCounterCount(CounterType.ARROW)).isEqualTo(1);
        // The counter lands on the Aura, never on the enchanted creature.
        assertThat(creature.getCounterCount(CounterType.ARROW)).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the Aura without arrow counters")
    void upkeepDeclinedAddsNoCounter() {
        addEnchantedCreature();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Archery Training").getCounterCount(CounterType.ARROW)).isZero();
    }

    // ===== Granted tap ability: X damage =====

    @Test
    @DisplayName("Enchanted creature taps to deal X damage where X is the Aura's arrow counter count")
    void grantedAbilityDealsAuraCounterDamage() {
        Permanent creature = addEnchantedCreature();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 2);

        // Goliath Beetle is a 3/1; 2 damage kills it.
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goliath Beetle");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The damage is exactly the arrow counter count, not the creature's")
    void grantedAbilityDamageEqualsAuraCounterCount() {
        addEnchantedCreature();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 1);

        // Capashen Templar has 2 toughness: 1 damage must not kill it.
        Permanent target = addCreatureReady(player2, new CapashenTemplar());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Capashen Templar");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two arrow counters deal two damage, killing a 2/2")
    void twoArrowCountersKillTwoToughnessCreature() {
        addEnchantedCreature();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 2);

        Permanent target = addCreatureReady(player2, new CapashenTemplar());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Capashen Templar");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addEnchantedCreature();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 1);

        Permanent bystander = addCreatureReady(player2, new CapashenTemplar());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void grantedAbilityCanTargetBlockingCreature() {
        addEnchantedCreature();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 1);

        Permanent target = addCreatureReady(player2, new CapashenTemplar());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setBlocking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("With no arrow counters, the granted ability deals no damage")
    void grantedAbilityDealsNoDamageWithNoArrowCounters() {
        Permanent creature = addEnchantedCreature();
        Permanent target = addCreatureReady(player2, new CapashenTemplar());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted tap ability cannot be activated while the enchanted creature is tapped")
    void grantedAbilityCannotBeActivatedWhileCreatureIsTapped() {
        Permanent creature = addEnchantedCreature();
        creature.tap();
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("The Aura's counters keep working after more counters accumulate")
    void damageScalesWithAccumulatedCounters() {
        addEnchantedCreature();
        Permanent aura = findPermanent(player1, "Archery Training");
        aura.setCounterCount(CounterType.ARROW, 3);

        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goliath Beetle");
    }

    // ===== Aura enchanting a creature =====

    @Test
    @DisplayName("Casting Archery Training targets a creature")
    void castingTargetsCreature() {
        Permanent creature = addCreatureReady(player1, new FledglingOsprey());
        harness.setHand(player1, List.of(new ArcheryTraining()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Archery Training")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Caltrops());
        harness.setHand(player1, List.of(new ArcheryTraining()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
