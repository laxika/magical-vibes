package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShanidSleepersScourge.class, AdelizTheCinderWind.class, GrizzlyBears.class,
        VolrathsStronghold.class, Forest.class})
class ShanidSleepersScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other legendary creatures you control menace")
    void grantsMenaceToOtherLegendaryCreatures() {
        harness.addToBattlefield(player1, new ShanidSleepersScourge());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AdelizTheCinderWind());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Shanid, Sleepers' Scourge"), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Adeliz, the Cinder Wind"), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Adeliz, the Cinder Wind"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Draws a card and loses 1 life when a legendary land is played")
    void triggersOnLegendaryLandPlay() {
        harness.addToBattlefield(player1, new ShanidSleepersScourge());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new VolrathsStronghold()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws a card and loses 1 life when a legendary spell is cast")
    void triggersOnLegendarySpellCast() {
        harness.addToBattlefield(player1, new ShanidSleepersScourge());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new AdelizTheCinderWind(), "{1}{U}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not trigger for nonlegendary cards")
    void ignoresNonlegendaryCards() {
        harness.addToBattlefield(player1, new ShanidSleepersScourge());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new AdelizTheCinderWind(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Adeliz, the Cinder Wind", "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting Shanid does not trigger its own battlefield ability")
    void doesNotTriggerForItsOwnCast() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new ShanidSleepersScourge(), "{1}{R}{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shanid, Sleepers' Scourge");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's legendary spell and legendary land do not trigger Shanid")
    void ignoresOpponentsLegendaryPlays() {
        harness.addToBattlefield(player1, new ShanidSleepersScourge());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VolrathsStronghold()));

        harness.playLand(player2, 0);
        harness.castFromHand(player2, new AdelizTheCinderWind(), "{1}{U}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Adeliz, the Cinder Wind");
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Putting a legendary land onto the battlefield does not count as playing it")
    void ignoresLegendaryLandEnteringWithoutBeingPlayed() {
        harness.addToBattlefield(player1, new ShanidSleepersScourge());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new VolrathsStronghold());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The cast trigger resolves before the legendary creature spell")
    void drawsBeforeLegendarySpellResolves() {
        harness.addToBattlefield(player1, new ShanidSleepersScourge());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new AdelizTheCinderWind(), "{1}{U}{R}");

        harness.assertLife(player1, 20);
        harness.assertNotInHand(player1, "Forest");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Adeliz, the Cinder Wind");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Adeliz, the Cinder Wind");
        harness.assertLife(player1, 19);
    }
}
