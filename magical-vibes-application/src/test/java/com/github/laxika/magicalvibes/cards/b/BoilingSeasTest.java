package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoilingSeas.class, GrizzlyBears.class, Island.class, Mountain.class, Plains.class})
class BoilingSeasTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new BoilingSeas(), "{3}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(BoilingSeas.class);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Destroys all Islands controlled by both players")
    void destroysAllIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new BoilingSeas(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Does not destroy other lands or creatures")
    void doesNotDestroyNonIslands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BoilingSeas(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Boiling Seas goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new BoilingSeas(), "{3}{R}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Boiling Seas");
    }
}
