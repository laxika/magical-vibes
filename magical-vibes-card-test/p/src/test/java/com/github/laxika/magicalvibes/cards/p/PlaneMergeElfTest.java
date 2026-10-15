package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
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
    @DisplayName("Landship creates no token and leaves a nonland on top after inspection")
    void landshipDoesNotRevealNonland() {
        harness.addToBattlefield(player1, new PlaneMergeElf());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
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
        resolveAllTriggers();

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
        resolveAllTriggers();

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
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elf.getPowerModifier()).isZero();
        assertThat(elf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Landship allows its controller to privately inspect a nonland")
    void landshipPrivatelyShowsNonland() {
        harness.addToBattlefield(player1, new PlaneMergeElf());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.clearMessages();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(harness.getConn1().getMessagesContaining(topCard.getName())).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining(topCard.getName())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Declining to reveal a land creates no token")
    void landshipCanDeclineReveal() {
        harness.addToBattlefield(player1, new PlaneMergeElf());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Landship does nothing with an empty library")
    void landshipHandlesEmptyLibrary() {
        harness.addToBattlefield(player1, new PlaneMergeElf());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Kinfall triggers when Plane-Merge Elf itself enters")
    void kinfallTriggersForItself() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlaneMergeElf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent elf = findPermanent(player1, "Plane-Merge Elf");
        assertThat(elf.getPowerModifier()).isEqualTo(1);
        assertThat(elf.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingBears.getPowerModifier()).isZero();
        assertThat(opposingBears.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed({TurnToFrog.class})
    @DisplayName("Kinfall does nothing if the entering creature no longer shares a type on resolution")
    void kinfallRechecksSharedCreatureType() {
        Permanent elf = addCreatureReady(player1, new PlaneMergeElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent warrior = findPermanent(player1, "Elvish Warrior");

        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());
        resolveAllTriggers();

        assertThat(elf.getPowerModifier()).isZero();
        assertThat(elf.getToughnessModifier()).isZero();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }
}
