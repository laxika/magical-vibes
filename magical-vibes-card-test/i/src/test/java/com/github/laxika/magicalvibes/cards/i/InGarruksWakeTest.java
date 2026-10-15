package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NissaWorldwaker;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SoulOfNewPhyrexia;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({InGarruksWake.class, RuneclawBear.class, NissaWorldwaker.class, Forest.class,
        SoulOfNewPhyrexia.class})
class InGarruksWakeTest extends BaseCardTest {

    private void castInGarruksWake() {
        harness.setHand(player1, List.of(new InGarruksWake()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Destroys opponents' creatures and planeswalkers")
    void destroysOpponentsCreaturesAndPlaneswalkers() {
        harness.addToBattlefield(player2, new RuneclawBear());
        addPlaneswalker(player2);

        castInGarruksWake();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Nissa, Worldwaker");
    }

    @Test
    @DisplayName("Leaves your permanents and opponents' noncreature nonplaneswalkers")
    void leavesYourPermanentsAndOtherPermanents() {
        harness.addToBattlefield(player1, new RuneclawBear());
        addPlaneswalker(player1);
        harness.addToBattlefield(player2, new RuneclawBear());
        addPlaneswalker(player2);
        harness.addToBattlefield(player2, new Forest());

        castInGarruksWake();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Nissa, Worldwaker");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Nissa, Worldwaker");
    }

    @Test
    @DisplayName("Destroys artifact creatures and puts destroyed permanents in the graveyard")
    void destroysArtifactCreatures() {
        harness.addToBattlefield(player2, new SoulOfNewPhyrexia());
        addPlaneswalker(player2);

        castInGarruksWake();

        harness.assertNotOnBattlefield(player2, "Soul of New Phyrexia");
        harness.assertInGraveyard(player2, "Soul of New Phyrexia");
        harness.assertInGraveyard(player2, "Nissa, Worldwaker");
        harness.assertInGraveyard(player1, "In Garruk's Wake");
    }

    @Test
    @DisplayName("Indestructible opponents' creatures and planeswalkers survive")
    void indestructiblePermanentsSurvive() {
        harness.addToBattlefield(player2, new SoulOfNewPhyrexia());
        harness.addToBattlefield(player2, new RuneclawBear());
        addPlaneswalker(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        castInGarruksWake();

        harness.assertOnBattlefield(player2, "Soul of New Phyrexia");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Nissa, Worldwaker");
        harness.assertNotInGraveyard(player2, "Soul of New Phyrexia");
        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
        harness.assertNotInGraveyard(player2, "Nissa, Worldwaker");
    }

    @Test
    @DisplayName("Resolves with no opposing creatures or planeswalkers")
    void resolvesWithoutMatchingPermanents() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new Forest());

        castInGarruksWake();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "In Garruk's Wake");
    }

    private void addPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new NissaWorldwaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
    }
}
