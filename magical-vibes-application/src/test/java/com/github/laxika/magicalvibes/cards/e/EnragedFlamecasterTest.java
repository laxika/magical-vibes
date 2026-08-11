package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class EnragedFlamecasterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater deals 2 damage to each opponent")
    void highManaValueSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting a spell with mana value 3 or less does not trigger")
    void lowManaValueSpellDoesNotDealDamage() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
