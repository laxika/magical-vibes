package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({DegaSanctuary.class, BlackKnight.class, GoblinRaider.class})
class DegaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when you control a black permanent only")
    void gainsTwoLifeWithBlackPermanentOnly() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new BlackKnight());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains 2 life when you control a red permanent only")
    void gainsTwoLifeWithRedPermanentOnly() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new GoblinRaider());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains 4 life when you control both a black and a red permanent")
    void gainsFourLifeWithBlackAndRedPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new GoblinRaider());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not gain life without a black or red permanent")
    void doesNotGainLifeWithoutBlackOrRedPermanent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
