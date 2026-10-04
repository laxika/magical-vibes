package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Clockspinning;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Tardis;
import com.github.laxika.magicalvibes.cards.w.WaningWurm;
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

@CardUsed({FleshDuplicate.class, GrizzlyBears.class, WaningWurm.class, Clockspinning.class, Tardis.class})
class FleshDuplicateTest extends BaseCardTest {

    @Test
    void copyingCreatureWithoutVanishingAddsThreeTimeCountersAndSacrifices() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent duplicate = castAndChoose(target);

        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(3);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate.getOriginalCard());
    }

    @Test
    void copyingCreatureWithVanishingUsesCopiedVanishing() {
        Permanent target = addCreatureReady(player2, new WaningWurm());
        target.setCounterCount(CounterType.TIME, 2);

        Permanent duplicate = castAndChoose(target);

        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(duplicate);
    }

    @Test
    void copiedVanishingStartsWithFreshCountersRatherThanCopyingExistingCounters() {
        Permanent target = addCreatureReady(player2, new WaningWurm());
        target.setCounterCount(CounterType.TIME, 1);

        Permanent duplicate = castAndChoose(target);

        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void copyingAnotherDuplicateCopiesItsVanishingWithoutAddingAnotherInstance() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent first = castAndChoose(creature);
        first.setCounterCount(CounterType.TIME, 1);

        Permanent second = castAndChoose(first);

        assertThat(second.getCounterCount(CounterType.TIME)).isEqualTo(3);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(second.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
    }

    @Test
    void decliningToCopyPutsDuplicateInGraveyard() {
        addCreatureReady(player2, new GrizzlyBears());
        FleshDuplicate card = new FleshDuplicate();
        harness.castFromHand(player1, card, "{U}{U}");
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void noCreatureToCopyPutsDuplicateInGraveyard() {
        FleshDuplicate card = new FleshDuplicate();
        harness.castFromHand(player1, card, "{U}{U}");
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void removingLastTimeCounterWithClockspinningSacrificesDuplicate() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent duplicate = castAndChoose(target);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(1);

        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, duplicate.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate.getOriginalCard());
    }

    @Test
    void copyingCrewedVehicleStillAddsVanishingToNoncreatureCopy() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new Tardis());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, crew.getId());
        }
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        Permanent duplicate = castAndChoose(vehicle);

        assertThat(gqs.isCreature(gd, duplicate)).isFalse();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(3);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    private Permanent castAndChoose(Permanent target) {
        FleshDuplicate card = new FleshDuplicate();
        harness.castFromHand(player1, card, "{U}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
