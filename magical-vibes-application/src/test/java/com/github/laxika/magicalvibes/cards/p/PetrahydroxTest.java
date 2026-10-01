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
        Permanent gelectrode = addCreatureReady(player2, new Gelectrode());

        harness.activateAbility(player2, 0, null, petrahydrox.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Petrahydrox");
        harness.assertInHand(player1, "Petrahydrox");
    }
}
