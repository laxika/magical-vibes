package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.cards.m.MagneticTheft;
import com.github.laxika.magicalvibes.cards.m.MarchOfOtherworldlyLight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BronzeCudgels.class, FangOfShigeki.class, MagneticTheft.class, MarchOfOtherworldlyLight.class})
class BronzeCudgelsTest extends BaseCardTest {

    @Test
    void pumpIncreasesWithEachResolution() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void resolutionCountResetsAtEndOfTurn() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(3);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isZero();
    }

    @Test
    void unattachedResolutionStillCounts() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        cudgels.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void equipAttachesToCreatureYouControl() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cudgels.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void firstResolutionOnNextTurnGrantsOnlyOnePower() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(creature.getPowerModifier()).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void stackedActivationsCountResolutionsRatherThanActivations() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(creature.getPowerModifier()).isZero();

        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void equipResolutionsDoNotIncreasePumpAmount() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(cudgels.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void separateEquipmentHasItsOwnResolutionCount() {
        Permanent first = addCreatureReady(player1, new BronzeCudgels());
        Permanent second = addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(4);
    }

    @Test
    void movingEquipmentKeepsEarlierBoostOnOriginalCreature() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent first = addCreatureReady(player1, new FangOfShigeki());
        Permanent second = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, second.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void pumpUsesAttachmentAtResolution() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent first = addCreatureReady(player1, new FangOfShigeki());
        Permanent second = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, List.of(cudgels.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void resolutionStillCountsWhenOriginallyEquippedCreatureLeaves() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent first = addCreatureReady(player1, new FangOfShigeki());
        Permanent second = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForXWithDiscards(player1, 0, 1, List.of(first.getId()), List.of());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, second.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(second.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void departedEquipmentUsesItsLastAttachment() {
        Permanent cudgels = addCreatureReady(player1, new BronzeCudgels());
        Permanent first = addCreatureReady(player1, new FangOfShigeki());
        Permanent second = addCreatureReady(player1, new FangOfShigeki());
        cudgels.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, List.of(cudgels.getId(), second.getId()));
        assertThat(cudgels.getAttachedTo()).isEqualTo(second.getId());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForXWithDiscards(player1, 0, 1, List.of(cudgels.getId()), List.of());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cudgels);
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void equipRejectsOpponentsCreature() {
        addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player2, new FangOfShigeki());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotRespondToPumpAbility() {
        addCreatureReady(player1, new BronzeCudgels());
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }
}
