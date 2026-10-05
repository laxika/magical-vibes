package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.Gelectrode;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Petrahydrox.class, Pyromatics.class, Gelectrode.class})
class PetrahydroxTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by a spell")
    void returnsToHandWhenTargetedBySpell() {
        Permanent petrahydrox = harness.addToBattlefieldAndReturn(player1, new Petrahydrox());

        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, petrahydrox.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Petrahydrox");
        harness.assertInHand(player1, "Petrahydrox");
    }

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by its controller's spell")
    void returnsToHandWhenTargetedByItsControllersSpell() {
        Permanent petrahydrox = harness.addToBattlefieldAndReturn(player1, new Petrahydrox());

        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, petrahydrox.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Petrahydrox");
        harness.assertInHand(player1, "Petrahydrox");
    }

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by an ability")
    void returnsToHandWhenTargetedByAbility() {
        Permanent petrahydrox = harness.addToBattlefieldAndReturn(player1, new Petrahydrox());
        addCreatureReady(player2, new Gelectrode());

        harness.activateAbility(player2, 0, null, petrahydrox.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Petrahydrox");
        harness.assertInHand(player1, "Petrahydrox");
    }

    @Test
    @DisplayName("Returns to its owner's hand when controlled by another player")
    void returnsToOwnerRatherThanController() {
        Petrahydrox card = new Petrahydrox();
        card.setOwnerId(player1.getId());
        Permanent petrahydrox = harness.addToBattlefieldAndReturn(player2, card);
        addCreatureReady(player1, new Gelectrode());

        harness.activateAbility(player1, 0, null, petrahydrox.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Petrahydrox");
        harness.assertInHand(player1, "Petrahydrox");
        harness.assertNotInHand(player2, "Petrahydrox");
    }

    @Test
    @DisplayName("Targeting does not return the creature before its trigger resolves")
    void returnUsesTheStack() {
        Permanent petrahydrox = harness.addToBattlefieldAndReturn(player1, new Petrahydrox());
        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, petrahydrox.getId());

        harness.assertOnBattlefield(player1, "Petrahydrox");
        harness.assertNotInHand(player1, "Petrahydrox");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Petrahydrox");
        harness.assertNotInGraveyard(player1, "Petrahydrox");
    }

    @Test
    @DisplayName("Does not return when a spell targets a player instead")
    void doesNotTriggerForAnUnrelatedTarget() {
        harness.addToBattlefield(player1, new Petrahydrox());
        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Petrahydrox");
        harness.assertNotInHand(player1, "Petrahydrox");
        harness.assertLife(player1, 19);
    }
}
