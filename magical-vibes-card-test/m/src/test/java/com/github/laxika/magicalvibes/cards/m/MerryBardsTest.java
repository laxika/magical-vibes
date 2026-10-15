package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerryBards.class, GrizzlyBears.class, TitanicGrowth.class})
class MerryBardsTest extends BaseCardTest {

    @Test
    void payingCreatesYoungHeroRoleOnControlledCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMerryBards();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds())
                .contains(target.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void decliningPaymentDoesNotCreateRole() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMerryBards();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
    }

    @Test
    void youngHeroRolePutsCounterOnAttackingCreatureWithToughnessThreeOrLess() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castMerryBards();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canAttachRoleToItselfWithNoOtherCreatures() {
        castMerryBards();
        Permanent bards = findPermanent(player1, "Merry Bards");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bards.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Young Hero").getAttachedTo()).isEqualTo(bards.getId());
    }

    @Test
    void payingQueuesSeparateRoleTriggerBeforeCreatingToken() {
        castMerryBards();
        Permanent bards = findPermanent(player1, "Merry Bards");
        harness.handleMayAbilityChosen(player1, true);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handlePermanentChosen(player1, bards.getId()));

        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Young Hero").getAttachedTo()).isEqualTo(bards.getId());
    }

    @Test
    void cannotCreateRoleWithoutManaForPayment() {
        harness.setHand(player1, List.of(new MerryBards()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void youngHeroRoleTriggersAtExactlyThreeToughness() {
        Permanent target = addCreatureReady(player1, new MerryBards());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        createRoleOn(target);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void youngHeroRoleDoesNotTriggerAboveThreeToughness() {
        Permanent target = addCreatureReady(player1, new MerryBards());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        createRoleOn(target);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void youngHeroRoleRechecksToughnessWhenAttackTriggerResolves() {
        Permanent target = addCreatureReady(player1, new MerryBards());
        createRoleOn(target);
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void newRoleReplacesExistingRoleControlledBySamePlayer() {
        Permanent target = addCreatureReady(player1, new MerryBards());
        createRoleOn(target);
        Permanent oldRole = findPermanent(player1, "Young Hero");

        createRoleOn(target);

        assertThat(findPermanents(player1, "Young Hero")).hasSize(1);
        Permanent newRole = findPermanent(player1, "Young Hero");
        assertThat(newRole.getId()).isNotEqualTo(oldRole.getId());
        assertThat(newRole.getAttachedTo()).isEqualTo(target.getId());
    }

    private void createRoleOn(Permanent target) {
        castMerryBards();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
    }

    private void castMerryBards() {
        harness.setHand(player1, List.of(new MerryBards()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
