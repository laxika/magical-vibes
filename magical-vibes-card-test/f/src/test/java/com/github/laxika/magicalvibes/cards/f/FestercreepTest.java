package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Festercreep.class, ElvishWarrior.class, CloakAndDagger.class})
class FestercreepTest extends BaseCardTest {


    @Test
    @DisplayName("Enters the battlefield with a +1/+1 counter (0/0 becomes 1/1)")
    void entersWithPlusOneCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Festercreep(), "{1}{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve any ETB processing

        Permanent creep = findCreep(player1);
        assertThat(creep.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creep.getEffectivePower()).isEqualTo(1);
        assertThat(creep.getEffectiveToughness()).isEqualTo(1);
    }


    @Test
    @DisplayName("Ability gives all other creatures -1/-1 but not Festercreep itself")
    void abilityDebuffsOtherCreatures() {
        Permanent creep = addReadyCreep(player1);
        creep.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ownElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent oppElf = addCreatureReady(player2, new ElvishWarrior());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Both other creatures get -1/-1
        assertThat(ownElf.getPowerModifier()).isEqualTo(-1);
        assertThat(ownElf.getToughnessModifier()).isEqualTo(-1);
        assertThat(oppElf.getPowerModifier()).isEqualTo(-1);
        assertThat(oppElf.getToughnessModifier()).isEqualTo(-1);

        harness.assertOnBattlefield(player1, "Festercreep");
        // Festercreep itself is not affected by the boost
        assertThat(creep.getPowerModifier()).isEqualTo(0);
        assertThat(creep.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability does not affect noncreature permanents")
    void abilityDoesNotAffectNoncreatures() {
        addReadyCreep(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new CloakAndDagger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(equipment.getPowerModifier()).isEqualTo(0);
        assertThat(equipment.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability removes a +1/+1 counter from Festercreep as cost")
    void abilityRemovesCounterAsCost() {
        Permanent creep = addReadyCreep(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(creep.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Festercreep");
        harness.passBothPriorities();

        assertThat(creep.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Debuff wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        addReadyCreep(player1);
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isEqualTo(0);
        assertThat(elf.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability when no +1/+1 counter remains")
    void cannotActivateWithoutCounter() {
        Permanent creep = addReadyCreep(player1);
        creep.setToughnessModifier(1);
        creep.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("A summoning-sick Festercreep can activate and resolve after dying")
    void activatesWhileSummoningSickAndResolvesAfterDying() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Festercreep(), "{1}{B}");
        harness.passBothPriorities();
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Festercreep");
        harness.assertInGraveyard(player1, "Festercreep");
        assertThat(elf.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        assertThat(elf.getPowerModifier()).isEqualTo(-1);
        assertThat(elf.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Other copies are affected and the affected creatures are fixed at resolution")
    void affectsOtherCopiesAndCreaturesPresentAtResolution() {
        addReadyCreep(player1);
        Permanent otherCreep = addReadyCreep(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.passBothPriorities();

        assertThat(otherCreep.getToughnessModifier()).isEqualTo(-1);
        harness.assertInGraveyard(player2, "Festercreep");
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(-1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(-1);
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player2, new ElvishWarrior());
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple activations stack and each removes its counter immediately")
    void multipleActivationsStack() {
        Permanent creep = addReadyCreep(player1);
        creep.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(creep.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(creep.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        resolveAllTriggers();

        assertThat(elf.getPowerModifier()).isEqualTo(-2);
        assertThat(elf.getToughnessModifier()).isEqualTo(-2);
        assertThat(creep.getPowerModifier()).isZero();
        assertThat(creep.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Festercreep");
    }

    private Permanent addReadyCreep(Player player) {
        Permanent perm = addCreatureReady(player, new Festercreep());
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return perm;
    }

    private Permanent findCreep(Player player) {
        return findPermanent(player, "Festercreep");
    }
}
