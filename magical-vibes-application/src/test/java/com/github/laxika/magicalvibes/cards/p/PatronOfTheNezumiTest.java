package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class PatronOfTheNezumiTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses 1 life when their permanent is put into their graveyard")
    void opponentLosesLifeWhenTheirPermanentDies() {
        harness.addToBattlefield(player1, new PatronOfTheNezumi());
        harness.addToBattlefield(player2, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Naturalize resolves, Patron triggers
        harness.passBothPriorities(); // trigger resolves

        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Controller loses no life when their own permanent is put into their graveyard")
    void controllerLosesNoLifeForOwnPermanent() {
        harness.addToBattlefield(player1, new PatronOfTheNezumi());
        harness.addToBattlefield(player1, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, mindStoneId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggers for any permanent type, including a dying creature")
    void triggersForDyingCreature() {
        harness.addToBattlefield(player1, new PatronOfTheNezumi());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Doom Blade resolves, Patron triggers
        harness.passBothPriorities(); // trigger resolves

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }
}
