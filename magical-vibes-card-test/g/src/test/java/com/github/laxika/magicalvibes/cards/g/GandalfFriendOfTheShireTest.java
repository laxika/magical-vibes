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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.passPriority(player2);

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
        harness.castAndResolveInstant(player1, 0, bears.getId());
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
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        harness.handlePermanentChosen(player1, gandalf.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsAgainWhenChoosingTheExistingRingBearer() {
        harness.addToBattlefield(player1, new GandalfFriendOfTheShire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing(), new SlipOnTheRing()));
        harness.setLibrary(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0,
                    harness.getPermanentId(player1, "Gandalf, Friend of the Shire"));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();

            assertThat(gd.ringStates.get(player1.getId()).bearerId()).isEqualTo(bears.getId());
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        }
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Divination);
    }

    @Test
    void doesNotDrawWhenTheOpponentIsTempted() {
        harness.addToBattlefield(player1, new GandalfFriendOfTheShire());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Divination()));
        harness.setHand(player2, List.of(new SlipOnTheRing()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotGrantFlashToOpponentsSorceries() {
        harness.addToBattlefield(player1, new GandalfFriendOfTheShire());
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantFlashToCreatureSpells() {
        harness.addToBattlefield(player1, new GandalfFriendOfTheShire());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastGandalfDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GandalfFriendOfTheShire()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gandalf, Friend of the Shire");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
