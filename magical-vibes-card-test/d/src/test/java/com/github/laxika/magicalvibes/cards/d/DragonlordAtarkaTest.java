package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.s.SarkhanUnbroken;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonlordAtarka.class, GarrukWildspeaker.class, GrizzlyBears.class,
        ColossodonYearling.class, SarkhanUnbroken.class})
class DragonlordAtarkaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB divides 5 damage among an opponent's creatures and planeswalkers")
    void etbDealsDividedDamageAmongOpponentPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 1, planeswalker.getId(), 4);

        castDragonlordAtarka(List.of(creature.getId(), planeswalker.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a creature its controller controls")
    void etbRejectsOwnCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker an opponent controls");
    }

    @Test
    @DisplayName("Damage division is chosen before opponents can respond to the ETB trigger")
    void damageDivisionIsChosenWhenTriggerGoesOnStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        prepareToCast();
        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));

        harness.passBothPriorities();

        PendingInteraction.ColorChoice division = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(division).isNotNull();
        assertThat(division.context()).isInstanceOf(ChoiceContext.CounterDistributionAssignment.class);
        assertThat(gd.stack).isEmpty();
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB can choose no targets even when legal targets exist")
    void etbCanChooseZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());

        castDragonlordAtarka(List.of());

        harness.assertOnBattlefield(player1, "Dragonlord Atarka");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Damage assigned to an illegal target is not redistributed")
    void remainingTargetReceivesOnlyItsOriginalAssignment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SarkhanUnbroken());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 2, planeswalker.getId(), 3);
        prepareToCast();
        harness.castCreature(player1, 0, List.of(creature.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castDragonlordAtarka(List<java.util.UUID> targetIds) {
        prepareToCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new DragonlordAtarka()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
