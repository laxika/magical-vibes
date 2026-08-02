package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class InGarruksWakeTest extends BaseCardTest {

    private void castInGarruksWake() {
        harness.setHand(player1, List.of(new InGarruksWake()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys opponents' creatures and planeswalkers")
    void destroysOpponentsCreaturesAndPlaneswalkers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addPlaneswalker(player2);

        castInGarruksWake();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Leaves your permanents and opponents' noncreature nonplaneswalkers")
    void leavesYourPermanentsAndOtherPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        addPlaneswalker(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addPlaneswalker(player2);
        harness.addToBattlefield(player2, new Forest());

        castInGarruksWake();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
    }

    private void addPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
    }
}
