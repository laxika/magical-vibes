package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectiveGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} copies a single-target spell cast at Reflective Golem")
    void payingCopiesSpellTargetingGolem() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, golem.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment does not copy the spell")
    void decliningPaymentDoesNotCopySpell() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, golem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).isEmpty();
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger Reflective Golem")
    void spellTargetingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ReflectiveGolem());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
