package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeyLine.class, FreshVolunteers.class, Forest.class, DeadlyInsect.class})
class LeyLineTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may put a +1/+1 counter on a creature of their choice")
    void activePlayerChoosesCreatureForCounter() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent activeCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(controllerCreature.getId(), activeCreature.getId())
                .doesNotContain(land.getId());
        harness.handlePermanentChosen(player2, activeCreature.getId());
        harness.passBothPriorities();

        assertThat(activeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(controllerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The active player may decline the counter")
    void activePlayerMayDecline() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent activeCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(activeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature with shroud is not offered as a target")
    void creatureWithShroudIsNotAValidTarget() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent validCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent shroudedCreature = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(validCreature.getId())
                .doesNotContain(shroudedCreature.getId());
    }

    @Test
    @DisplayName("Accepting with no creature targets resolves without putting on a counter")
    void acceptingWithoutCreatureTargetDoesNothing() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
