package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ExaltedFlamerOfTzeentch.class, GrizzlyBears.class, Shock.class})
class ExaltedFlamerOfTzeentchTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep returns a random instant or sorcery from the graveyard")
    void upkeepReturnsInstantOrSorcery() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting an instant deals 1 damage to each opponent")
    void castingInstantDamagesEachOpponent() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Casting a creature does not trigger damage")
    void castingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ExaltedFlamerOfTzeentch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
