package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraHeartOfFire;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumOfShatteredHeights.class, SanctumOfCalmWaters.class, ColossalDreadmaw.class,
        ChandraHeartOfFire.class, Mountain.class, Forest.class})
class SanctumOfShatteredHeightsTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a land deals damage equal to the Shrines you control")
    void discardingLandDealsDamageEqualToShrines() {
        addSanctumAndShrine();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Discarding a Shrine deals damage to a planeswalker")
    void discardingShrineDealsDamageToPlaneswalker() {
        addSanctumAndShrine();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHeartOfFire());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SanctumOfCalmWaters()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Sanctum of Calm Waters");
    }

    @Test
    @DisplayName("The ability cannot discard a nonland, non-Shrine card")
    void cannotActivateWithoutMatchingDiscardCard() {
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature, nonplaneswalker permanent")
    void cannotTargetNonCreatureNonPlaneswalker() {
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void countsOnlyControlledShrinesIncludingItself() {
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        harness.addToBattlefield(player2, new SanctumOfCalmWaters());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void countsShrinesAtResolutionRatherThanActivation() {
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void resolvesAfterSourceLeavesWithoutCountingIt() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SanctumOfShatteredHeights());
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void dealsNoDamageWhenNoShrinesRemain() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SanctumOfShatteredHeights());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    void canActivateTwiceWithoutTapping() {
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSanctumAndShrine() {
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
    }
}
