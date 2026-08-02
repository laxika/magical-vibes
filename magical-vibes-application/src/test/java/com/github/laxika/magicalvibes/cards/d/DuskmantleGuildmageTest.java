package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuskmantleGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Second ability mills two cards from target player's library")
    void millsTwoCards() {
        addGuildmage(player1);
        addManaFor(player1, 4);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("First ability drains 1 life for each card put into an opponent's graveyard")
    void drainsForEachCardInOpponentGraveyard() {
        addGuildmage(player1);
        addManaFor(player1, 3);
        addManaFor(player1, 4);

        activateDrain();

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveStack();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("First ability ignores cards put into the controller's own graveyard")
    void ignoresOwnGraveyard() {
        addGuildmage(player1);
        addManaFor(player1, 3);
        addManaFor(player1, 4);

        activateDrain();

        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        resolveStack();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Two activations of the first ability drain twice per card")
    void activationsStack() {
        addGuildmage(player1);
        addManaFor(player1, 3);
        addManaFor(player1, 3);
        addManaFor(player1, 4);

        activateDrain();
        activateDrain();

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveStack();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("First ability fires for cards entering the graveyard from the battlefield")
    void firesForDyingCreature() {
        addGuildmage(player1);
        addManaFor(player1, 3);

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        activateDrain();

        int lifeBefore = gd.getLife(player2.getId());

        killWithShock(bears);
        resolveStack();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("First ability stops draining after the turn ends")
    void wearsOffAtEndOfTurn() {
        addGuildmage(player1);
        addManaFor(player1, 3);

        activateDrain();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player2.getId());

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        killWithShock(bears);
        resolveStack();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    private void activateDrain() {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    /** Resolves everything on the stack, including the life-loss triggers the mill puts there. */
    private void resolveStack() {
        for (int i = 0; i < 10 && !gd.stack.isEmpty(); i++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }

    /** Puts the creature into its owner's graveyard through the normal death pipeline. */
    private void killWithShock(Permanent creature) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private Permanent addGuildmage(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DuskmantleGuildmage());
    }

    private void addManaFor(Player player, int generic) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.WHITE, generic - 2);
    }
}
