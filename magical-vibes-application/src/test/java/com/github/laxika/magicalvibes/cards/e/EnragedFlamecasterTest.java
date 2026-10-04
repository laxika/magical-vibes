package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({EnragedFlamecaster.class, GrizzlyBears.class, HillGiant.class, JacesIngenuity.class})
class EnragedFlamecasterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater deals 2 damage to each opponent")
    void highManaValueSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new JacesIngenuity(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting a spell with mana value 3 or less does not trigger")
    void lowManaValueSpellDoesNotDealDamage() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A spell with mana value exactly four triggers before the spell resolves")
    void manaValueFourTriggers() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("A spell with mana value exactly three does not trigger")
    void manaValueThreeDoesNotTrigger() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new EnragedFlamecaster(), "{2}{R}");
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's high mana value spell does not trigger")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new JacesIngenuity(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Flamecaster triggers independently for the same spell")
    void multipleFlamecastersEachDealDamage() {
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.addToBattlefield(player1, new EnragedFlamecaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }
}
