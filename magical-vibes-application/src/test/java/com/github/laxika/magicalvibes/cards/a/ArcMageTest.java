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

    @Test
    @DisplayName("Each chosen target must be assigned at least one damage")
    void cannotAssignZeroDamageToSecondTarget() {
        addReadyArcMage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 2, player2.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage assigned to a departed target is not redistributed")
    void remainingTargetReceivesOnlyAssignedDamage() {
        addReadyArcMage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 1, player2.getId(), 1));
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    @DisplayName("Arc Mage can target itself and dies from its own two damage")
    void canDealLethalDamageToItself() {
        Permanent mage = addReadyArcMage();
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(mage.getId(), 2));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arc Mage");
        harness.assertInGraveyard(player1, "Flowstone Crusher");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mage);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ArcMage());
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent mage = addReadyArcMage();
        mage.tap();
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the required red mana")
    void cannotActivateWithoutRedMana() {
        addReadyArcMage();
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability still deals damage after Arc Mage leaves the battlefield")
    void dealsDamageAfterSourceLeavesBattlefield() {
        Permanent mage = addReadyArcMage();
        harness.setHand(player1, List.of(new FlowstoneCrusher()));
        addAbilityMana();
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(player2.getId(), 2));
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(mage);
        gd.playerGraveyards.get(player1.getId()).add(mage.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
        harness.assertInGraveyard(player1, "Arc Mage");
        harness.assertInGraveyard(player1, "Flowstone Crusher");
    }

    private Permanent addReadyArcMage() {
        return addCreatureReady(player1, new ArcMage());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
