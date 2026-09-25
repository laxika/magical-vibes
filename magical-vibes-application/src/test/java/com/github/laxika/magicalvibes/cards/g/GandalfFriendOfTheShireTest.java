package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.SlipOnTheRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfFriendOfTheShire.class, Divination.class, SlipOnTheRing.class,
        GrizzlyBears.class})
class GandalfFriendOfTheShireTest extends BaseCardTest {

    @Test
    void canCastSorceryDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GandalfFriendOfTheShire());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    void drawsWhenAnotherCreatureBecomesRingBearer() {
        harness.addToBattlefield(player1, new GandalfFriendOfTheShire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.setLibrary(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenGandalfBecomesRingBearer() {
        harness.addToBattlefield(player1, new GandalfFriendOfTheShire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.setLibrary(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent gandalf = findPermanent(player1, "Gandalf, Friend of the Shire");
        harness.castInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gandalf.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
