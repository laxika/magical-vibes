package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyFlicker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThousandWinds.class, GrizzlyBears.class, SerraAngel.class, Island.class, GhostlyFlicker.class})
class ThousandWindsTest extends BaseCardTest {

    @Test
    void morphingReturnsOtherTappedCreaturesToTheirOwnersHands() {
        Permanent tappedOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedOwn.tap();
        harness.addToBattlefield(player1, new SerraAngel());
        Permanent tappedOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOpponent.tap();
        harness.addToBattlefield(player2, new SerraAngel());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Island());
        tappedLand.tap();
        harness.setHand(player1, List.of(new ThousandWinds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thousandWinds = findPermanent(player1, "Thousand Winds");
        thousandWinds.tap();
        assertThat(thousandWinds.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(thousandWinds));
        harness.passBothPriorities();

        assertThat(thousandWinds.isFaceDown()).isFalse();
        assertThat(thousandWinds.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void castingFaceUpDoesNotReturnTappedCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new ThousandWinds()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thousand Winds");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerChecksTappedStatusWhenItResolves() {
        Permanent initiallyTapped = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        initiallyTapped.tap();
        Permanent initiallyUntapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent winds = castFaceDownWinds();

        turnWindsFaceUp(winds);
        assertThat(winds.isFaceDown()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(initiallyTapped);
        initiallyTapped.untap();
        initiallyUntapped.tap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(initiallyTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(initiallyUntapped);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void turningFaceUpWithNoOtherTappedCreaturesResolvesNormally() {
        Permanent winds = castFaceDownWinds();

        turnWindsFaceUp(winds);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thousand Winds");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsStolenCreatureToItsOwnerRatherThanItsController() {
        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, bearsCard);
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        bears.tap();
        Permanent winds = castFaceDownWinds();

        turnWindsFaceUp(winds);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsAnotherTappedThousandWinds() {
        Permanent otherWinds = harness.addToBattlefieldAndReturn(player2, new ThousandWinds());
        otherWinds.tap();
        Permanent winds = castFaceDownWinds();

        turnWindsFaceUp(winds);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thousand Winds");
        harness.assertNotOnBattlefield(player2, "Thousand Winds");
        harness.assertInHand(player2, "Thousand Winds");
    }

    @Test
    void blinkedSourceIsANewPermanentAndIsReturnedIfTapped() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent winds = castFaceDownWinds();
        turnWindsFaceUp(winds);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, List.of(winds.getId(), island.getId()));
        Permanent returnedWinds = findPermanent(player1, "Thousand Winds");
        assertThat(returnedWinds.getId()).isNotEqualTo(winds.getId());
        assertThat(gd.stack).hasSize(1);
        returnedWinds.tap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thousand Winds");
        harness.assertInHand(player1, "Thousand Winds");
    }

    private Permanent castFaceDownWinds() {
        harness.setHand(player1, List.of(new ThousandWinds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Thousand Winds");
    }

    private void turnWindsFaceUp(Permanent winds) {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(winds));
    }
}
