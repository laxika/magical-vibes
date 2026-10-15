package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflectiveGolem.class, GiantGrowth.class, HillGiant.class, ExpandedAnatomy.class, SeedsOfStrength.class})
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

    @Test
    void canChooseAnotherCreatureForTheCopy() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ReflectiveGolem());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, golem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCastingAtGolemDoesNotTrigger() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, golem.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void sorceryCopyResolvesWithOriginalTargets() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, golem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedTargetsOnOnlyGolemStillTrigger() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(golem.getId(), golem.getId(), golem.getId()));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    void targetingGolemAndAnotherCreatureDoesNotTrigger() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ReflectiveGolem());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(golem.getId(), golem.getId(), other.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
