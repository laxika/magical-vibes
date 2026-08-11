package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CosisTricksterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger puts a +1/+1 counter on Cosi's Trickster")
    void acceptingTriggerAddsCounter() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player1, new CosisTrickster());

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());
        beginMayAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger puts no counter on Cosi's Trickster")
    void decliningTriggerAddsNoCounter() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player1, new CosisTrickster());

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());
        beginMayAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Shuffling your own library does not trigger Cosi's Trickster")
    void ownShuffleDoesNotTrigger() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player1, new CosisTrickster());

        LibraryShuffleHelper.shuffleLibrary(gd, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void beginMayAbility() {
        PendingMayAbility pending = gd.pendingMayAbilities.getFirst();
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(
                pending.controllerId(), pending.description(), pending.manaCost()));
    }
}
