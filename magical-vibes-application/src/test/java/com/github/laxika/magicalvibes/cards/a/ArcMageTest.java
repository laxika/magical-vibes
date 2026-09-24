package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FlowstoneCrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcMage.class, FlowstoneCrusher.class})
class ArcMageTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{R}, {T}, and discarding a card deals 2 damage to one target")
    void dealsTwoDamageToOneTarget() {
        Permanent mage = addReadyArcMage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 2));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Flowstone Crusher");
    }

    @Test
    @DisplayName("The ability divides 2 damage between two targets")
    void dividesDamageBetweenTwoTargets() {
        addReadyArcMage();
        Permanent creatureTarget = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();

        harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(creatureTarget.getId(), 1, player2.getId(), 1));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(creatureTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Cannot activate without choosing a target")
    void cannotActivateWithoutChoosingTarget() {
        addReadyArcMage();
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Damage assignments must sum to 2");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        addReadyArcMage();
        harness.setHand(player1, List.of());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArcMage() {
        return addCreatureReady(player1, new ArcMage());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
