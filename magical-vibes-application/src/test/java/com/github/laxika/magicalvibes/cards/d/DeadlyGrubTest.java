package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed(DeadlyGrub.class)
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
}
