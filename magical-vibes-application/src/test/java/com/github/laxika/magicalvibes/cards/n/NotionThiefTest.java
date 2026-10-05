package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.t.TurnBurn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NotionThief.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class, TurnBurn.class})
class NotionThiefTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's extra draw is skipped and the controller draws instead")
    void stealsOpponentExtraDraw() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The opponent's first draw-step draw is not stolen")
    void doesNotStealDrawStepDraw() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island()));

        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Island");
    }

    @Test
    @DisplayName("A second draw during the opponent's draw step is stolen")
    void stealsSecondDrawStepDraw() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island()));

        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DRAW);
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("The controller's own draws are unaffected")
    void doesNotAffectControllerDraws() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
    }
    @Test
    @DisplayName("Opposing Notion Thieves return an extra draw to the original player")
    void opposingThievesReturnDrawToOriginalPlayer() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.addToBattlefield(player2, new NotionThief());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A Notion Thief that loses all abilities does not steal draws")
    void abilityLossStopsStealingDraws() {
        var thief = harness.addToBattlefieldAndReturn(player1, new NotionThief());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new TurnBurn(), new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, 0, thief.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Thieves controlled by one player steal each draw only once")
    void sameControllerThievesDoNotMultiplyDraws() {
        harness.addToBattlefield(player1, new NotionThief());
        harness.addToBattlefield(player1, new NotionThief());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
