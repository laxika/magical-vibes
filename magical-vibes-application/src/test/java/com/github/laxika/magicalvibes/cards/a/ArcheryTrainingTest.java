package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcheryTrainingTest extends BaseCardTest {

    private Permanent addEnchantedBears() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new ArcheryTraining());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return bearsPerm;
    }

    // ===== Upkeep trigger: arrow counter =====

    @Test
    @DisplayName("Accepting the upkeep trigger puts an arrow counter on the Aura")
    void upkeepAcceptedAddsArrowCounter() {
        Permanent aura = addEnchantedBears();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent auraPerm = findPermanent(player1, "Archery Training");
        assertThat(auraPerm.getCounterCount(CounterType.ARROW)).isEqualTo(1);
        // The counter lands on the Aura, never on the enchanted creature.
        assertThat(aura.getCounterCount(CounterType.ARROW)).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the Aura without arrow counters")
    void upkeepDeclinedAddsNoCounter() {
        addEnchantedBears();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Archery Training").getCounterCount(CounterType.ARROW)).isZero();
    }

    // ===== Granted tap ability: X damage =====

    @Test
    @DisplayName("Enchanted creature taps to deal X damage where X is the Aura's arrow counter count")
    void grantedAbilityDealsAuraCounterDamage() {
        Permanent bearsPerm = addEnchantedBears();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 2);

        // Llanowar Elves is a 1/1; 2 damage kills it.
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The damage is exactly the arrow counter count, not the creature's")
    void grantedAbilityDamageEqualsAuraCounterCount() {
        addEnchantedBears();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 1);

        // Grizzly Bears has 2 toughness: 1 damage must not kill it.
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two arrow counters deal two damage, killing a 2/2")
    void twoArrowCountersKillTwoToughnessCreature() {
        addEnchantedBears();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 2);

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addEnchantedBears();
        findPermanent(player1, "Archery Training").setCounterCount(CounterType.ARROW, 1);

        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("The Aura's counters keep working after more counters accumulate")
    void damageScalesWithAccumulatedCounters() {
        addEnchantedBears();
        Permanent aura = findPermanent(player1, "Archery Training");
        aura.setCounterCount(CounterType.ARROW, 3);

        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        target.setAttacking(true);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    // ===== Aura enchanting a creature =====

    @Test
    @DisplayName("Casting Archery Training targets a creature")
    void castingTargetsCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ArcheryTraining()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Archery Training")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        harness.setHand(player1, java.util.List.of(new ArcheryTraining()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
