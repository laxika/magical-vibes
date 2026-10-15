package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FuryCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadlyGrub.class, FuryCharm.class})
class DeadlyGrubTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters")
    void entersWithTimeCounters() {
        harness.setHand(player1, List.of(new DeadlyGrub()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Deadly Grub").getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(grub.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grub);
    }

    @Test
    @DisplayName("Does not remove a time counter during an opponent's upkeep")
    void opponentUpkeepDoesNotRemoveTimeCounter() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(grub.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does nothing during upkeep when it has no time counters")
    void upkeepDoesNothingWithoutTimeCounters() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Deadly Grub")).containsExactly(grub);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Deadly Grub");
        harness.assertInGraveyard(player1, "Deadly Grub");
    }

    @Test
    @DisplayName("Creates an Insect after vanishing sacrifices it")
    void createsInsectAfterVanishingSacrifice() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Insect")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a shrouded 6/1 Insect when it dies without time counters")
    void createsInsectWhenItDiesWithoutTimeCounters() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, grub));
        harness.passBothPriorities();

        List<Permanent> insects = findPermanents(player1, "Insect");
        assertThat(insects).hasSize(1);
        assertThat(insects.getFirst().getEffectivePower()).isEqualTo(6);
        assertThat(insects.getFirst().getEffectiveToughness()).isEqualTo(1);
        assertThat(insects.getFirst().getCard().hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Does not create an Insect when it dies with time counters")
    void doesNotCreateInsectWhenItDiesWithTimeCounters() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, grub));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    @Test
    @DisplayName("Vanishing does not trigger at upkeep without time counters")
    void noUpkeepTriggerWithoutTimeCounters() {
        addCreatureReady(player1, new DeadlyGrub());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Deadly Grub");
    }

    @Test
    @DisplayName("Removing the last time counters with Fury Charm triggers sacrifice and creates an Insect")
    void externalRemovalOfLastTimeCountersCausesSacrifice() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new FuryCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 2, grub.getId());
        harness.passBothPriorities();

        assertThat(grub.getCounterCount(CounterType.TIME)).isZero();
        harness.assertOnBattlefield(player1, "Deadly Grub");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Deadly Grub");
        harness.assertInGraveyard(player1, "Deadly Grub");
        assertThat(findPermanents(player1, "Insect")).hasSize(1);
    }

    @Test
    @DisplayName("Removing time counters with Fury Charm does not sacrifice while a counter remains")
    void externalRemovalLeavesGrubAliveWhenTimeCounterRemains() {
        Permanent grub = addCreatureReady(player1, new DeadlyGrub());
        grub.setCounterCount(CounterType.TIME, 3);
        harness.setHand(player1, List.of(new FuryCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 2, grub.getId());
        resolveAllTriggers();

        assertThat(grub.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Deadly Grub");
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }
}
