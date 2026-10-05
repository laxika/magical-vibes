package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeyLine.class, FreshVolunteers.class, Forest.class, DeadlyInsect.class, Vendetta.class, TrollAscetic.class})
class LeyLineTest extends BaseCardTest {

    @Test
    @DisplayName("The active player chooses a target before responses and may add the counter at resolution")
    void activePlayerChoosesCreatureForCounter() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent activeCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(controllerCreature.getId(), activeCreature.getId())
                .doesNotContain(land.getId());
        harness.handlePermanentChosen(player2, activeCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(activeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(controllerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The active player may decline the counter after choosing the target")
    void activePlayerMayDecline() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent activeCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player2, activeCreature.getId());
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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(validCreature.getId())
                .doesNotContain(shroudedCreature.getId());
        harness.handlePermanentChosen(player2, validCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(validCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shroudedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With no legal creature target the trigger is removed without an optional choice")
    void noCreatureTargetRemovesTrigger() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("On the controller's upkeep they may choose an opponent's creature")
    void controllerMayPutCounterOnOpponentsCreature() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With only a shrouded creature the trigger has no legal target")
    void onlyShroudedCreatureRemovesTrigger() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing the chosen target in response prevents the counter and optional choice")
    void removedTargetCannotBeReplacedAtResolution() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player2, target.getId());

        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Fresh Volunteers");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    @Test
    @DisplayName("Hexproof depends on Ley Line's controller even when the opponent chooses the target")
    void hexproofUsesAbilityControllerRatherThanChoosingPlayer() {
        harness.addToBattlefield(player1, new LeyLine());
        Permanent controllersTroll = harness.addToBattlefieldAndReturn(player1, new TrollAscetic());
        Permanent activePlayersTroll = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());
        harness.addToBattlefield(player2, new FreshVolunteers());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(controllersTroll.getId())
                .doesNotContain(activePlayersTroll.getId());
        harness.handlePermanentChosen(player2, controllersTroll.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(controllersTroll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(activePlayersTroll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}