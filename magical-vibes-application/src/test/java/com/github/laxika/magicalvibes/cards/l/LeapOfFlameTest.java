package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeapOfFlame.class, Gristleback.class, IzzetSignet.class})
class LeapOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+0, flying, and first strike")
    void givesTargetCreatureBoostAndKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        castLeapOfFlame(target.getId(), List.of());

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        castLeapOfFlame(target.getId(), List.of("{U}{R}", "{U}{R}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Replicate copy may choose a new creature target")
    void replicateCopyMayTargetAnotherCreature() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        castLeapOfFlame(originalTarget.getId(), List.of("{U}{R}"));

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.getPowerModifier()).isEqualTo(1);
        assertThat(originalTarget.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.FIRST_STRIKE);
        assertThat(newTarget.getPowerModifier()).isEqualTo(1);
        assertThat(newTarget.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Replicate payments require the full {U}{R} cost")
    void cannotPayReplicateWithoutEnoughMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        harness.setHand(player1, List.of(new LeapOfFlame()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithRepeatedCosts(
                player1, 0, target.getId(), List.of("{U}{R}")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and keywords wear off at cleanup")
    void wearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        castLeapOfFlame(target.getId(), List.of());

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new IzzetSignet()).getId();
        harness.setHand(player1, List.of(new LeapOfFlame()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("A replicate copy can target another creature after the original target is sacrificed")
    void replicateCanRetargetAfterOriginalTargetLeaves() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        castLeapOfFlame(originalTarget.getId(), List.of("{U}{R}"));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Gristleback");
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        resolveAllTriggers();

        assertThat(newTarget.getPowerModifier()).isEqualTo(1);
        assertThat(newTarget.getToughnessModifier()).isZero();
        assertThat(newTarget.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.FIRST_STRIKE);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Leap of Flame");
    }

    @Test
    @DisplayName("A spell whose target is sacrificed does not boost another creature")
    void removedTargetReceivesNoEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        castLeapOfFlame(target.getId(), List.of());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gristleback");
        harness.assertInGraveyard(player1, "Leap of Flame");
        assertThat(otherCreature.getPowerModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
        assertThat(otherCreature.getGrantedKeywords()).doesNotContain(Keyword.FLYING, Keyword.FIRST_STRIKE);
        assertThat(gd.stack).isEmpty();
    }

    private void castLeapOfFlame(UUID targetId, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new LeapOfFlame()));
        harness.addMana(player1, ManaColor.BLUE, 1 + replicatePayments.size());
        harness.addMana(player1, ManaColor.RED, 1 + replicatePayments.size());
        harness.castInstantWithRepeatedCosts(player1, 0, targetId, replicatePayments);
    }
}
