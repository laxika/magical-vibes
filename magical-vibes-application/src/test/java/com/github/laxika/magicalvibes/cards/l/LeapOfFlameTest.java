package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({LeapOfFlame.class, GrizzlyBears.class, Plains.class})
class LeapOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+0, flying, and first strike")
    void givesTargetCreatureBoostAndKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castLeapOfFlame(target.getId(), List.of());

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
    @DisplayName("The boost and keywords wear off at cleanup")
    void wearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        harness.addToBattlefield(player2, new Plains());
        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.setHand(player1, List.of(new LeapOfFlame()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castLeapOfFlame(UUID targetId, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new LeapOfFlame()));
        harness.addMana(player1, ManaColor.BLUE, 1 + replicatePayments.size());
        harness.addMana(player1, ManaColor.RED, 1 + replicatePayments.size());
        harness.castInstantWithRepeatedCosts(player1, 0, targetId, replicatePayments);
    }
}
