package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class DarkRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("When Dark Revenant dies, its trigger puts it from the graveyard on top of its owner's library")
    void diesThenTriggerPutsItOnTopOfLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, new DarkRevenant());
        // Mark lethal damage on the 2/2 and let state-based actions destroy it.
        revenant.setMarkedDamage(2);

        harness.runStateBasedActions();

        // It first enters the graveyard, then its death trigger waits on the stack.
        harness.assertInGraveyard(player1, "Dark Revenant");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Dark Revenant");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Dark Revenant");
    }
}
