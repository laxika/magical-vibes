package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlaneMergeElf.class, ElvishWarrior.class, Forest.class, GrizzlyBears.class})
class PlaneMergeElfTest extends BaseCardTest {

    @Test
    @DisplayName("Landship offers a land on top and creates an Elf Warrior when revealed")
    void landshipCreatesElfWarrior() {
        PlaneMergeElf elf = new PlaneMergeElf();
        harness.addToBattlefield(player1, elf);
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
    }

    @Test
    @DisplayName("Landship does not prompt for a nonland top card")
    void landshipIgnoresNonland() {
        harness.addToBattlefield(player1, new PlaneMergeElf());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Kinfall pumps your creatures when a shared-type creature enters")
    void kinfallPumpsSharedTypeCreatures() {
        Permanent elf = addCreatureReady(player1, new PlaneMergeElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isEqualTo(1);
        assertThat(elf.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kinfall ignores creatures without a shared type")
    void kinfallIgnoresUnrelatedCreature() {
        Permanent elf = addCreatureReady(player1, new PlaneMergeElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isZero();
        assertThat(elf.getToughnessModifier()).isZero();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Kinfall's temporary pump wears off at cleanup")
    void kinfallPumpWearsOff() {
        Permanent elf = addCreatureReady(player1, new PlaneMergeElf());
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isZero();
        assertThat(elf.getToughnessModifier()).isZero();
    }
}
