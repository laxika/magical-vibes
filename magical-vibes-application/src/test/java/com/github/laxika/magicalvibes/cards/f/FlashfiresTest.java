package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plateau;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flashfires.class, GrizzlyBears.class, Island.class, Mountain.class, Plateau.class, Plains.class})
class FlashfiresTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Flashfires(), "{3}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Flashfires.class);
    }

    @Test
    @DisplayName("Destroys all Plains controlled by both players")
    void destroysAllPlains() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        castFlashfiresAndResolve();

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("Does not destroy other lands or creatures")
    void doesNotDestroyNonPlains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFlashfiresAndResolve();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys nonbasic lands with the Plains subtype")
    void destroysNonbasicPlains() {
        harness.addToBattlefield(player1, new Plateau());
        castFlashfiresAndResolve();

        harness.assertNotOnBattlefield(player1, "Plateau");
        harness.assertInGraveyard(player1, "Plateau");
    }

    @Test
    @DisplayName("Indestructible Plains survives Flashfires")
    void indestructiblePlainsSurvives() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        plains.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castFlashfiresAndResolve();

        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("Flashfires goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castFlashfiresAndResolve();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flashfires");
    }

    private void castFlashfiresAndResolve() {
        harness.castFromHand(player1, new Flashfires(), "{3}{R}");
        harness.passBothPriorities();
    }
}
