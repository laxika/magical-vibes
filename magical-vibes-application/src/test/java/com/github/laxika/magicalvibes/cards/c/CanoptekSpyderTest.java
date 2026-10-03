package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanoptekSpyder.class, Forest.class, Ornithopter.class, SkySkiff.class,
        AccordersShield.class, GrizzlyBears.class, WilyGoblin.class, CanoptekScarabSwarm.class, CanoptekWraith.class, SolRing.class, MarchOfTheMachines.class})
class CanoptekSpyderTest extends BaseCardTest {

    @Test
    void anotherNontokenArtifactCreatureOrVehicleEnteringDrawsACard() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card creatureDraw = new Forest();
        Card vehicleDraw = new Forest();
        harness.setLibrary(player1, List.of(creatureDraw, vehicleDraw));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new SkySkiff());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creatureDraw, vehicleDraw);
    }

    @Test
    void otherArtifactAndCreatureTypesDoNotTrigger() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new AccordersShield());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void tokenArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new WilyGoblin());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void enteringSpyderDoesNotTriggerItself() {
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CanoptekSpyder());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void opponentsArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player2, new CanoptekWraith());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void anotherSpyderTriggersEachExistingSpyderOnce() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card remainingCard = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remainingCard));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CanoptekSpyder());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void artifactCreatureTokensDoNotDrawAdditionalCards() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        harness.setGraveyard(player2, List.of(new Forest(), new SolRing()));
        Card drawnCard = new Forest();
        Card remainingCard = new Forest();
        Card secondRemainingCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard, secondRemainingCard));
        harness.setHand(player1, List.of(new CanoptekScarabSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard, secondRemainingCard);
    }

    @Test
    void artifactEnteringAsACreatureDueToMarchOfTheMachinesDrawsACard() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new SolRing());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
