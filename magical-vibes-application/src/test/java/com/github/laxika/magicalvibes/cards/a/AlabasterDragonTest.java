package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class AlabasterDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Alabaster Dragon dies, a triggered ability shuffles it from the graveyard into its owner's library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new AlabasterDragon());
        // Mark lethal damage on the 4/4 and let state-based actions destroy it.
        dragon.setMarkedDamage(4);

        harness.runStateBasedActions();

        // It first enters the graveyard, then its death trigger waits on the stack.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Alabaster Dragon"));
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        // After the trigger resolves, the dragon is shuffled into its owner's library.
        harness.assertNotInGraveyard(player1, "Alabaster Dragon");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Alabaster Dragon"));
    }
}
