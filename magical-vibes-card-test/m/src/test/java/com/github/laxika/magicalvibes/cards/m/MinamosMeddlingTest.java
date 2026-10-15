package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrackTheEarth;
import com.github.laxika.magicalvibes.cards.g.GoryosVengeance;
import com.github.laxika.magicalvibes.cards.i.IwamoriOfTheOpenFist;
import com.github.laxika.magicalvibes.cards.i.IsaoEnlightenedBushi;
import com.github.laxika.magicalvibes.cards.i.IntiSeneschalOfTheSun;
import com.github.laxika.magicalvibes.cards.n.NephaliaAcademy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinamosMeddling.class, CrackTheEarth.class, GoryosVengeance.class, IwamoriOfTheOpenFist.class,
        IsaoEnlightenedBushi.class, IntiSeneschalOfTheSun.class, NephaliaAcademy.class})
class MinamosMeddlingTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and discards hand cards sharing a spliced card's name")
    void countersAndDiscardsSplicedNames() {
        IwamoriOfTheOpenFist graveyardLegend = new IwamoriOfTheOpenFist();
        harness.setGraveyard(player1, List.of(graveyardLegend));
        CrackTheEarth crackTheEarth = new CrackTheEarth();
        GoryosVengeance splicedVengeance = new GoryosVengeance();
        GoryosVengeance secondVengeance = new GoryosVengeance();
        harness.setHand(player1, List.of(crackTheEarth, splicedVengeance, secondVengeance));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, graveyardLegend.getId(), List.of(1));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, crackTheEarth.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Crack the Earth");
        harness.assertNotOnBattlefield(player1, "Iwamori of the Open Fist");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Goryo's Vengeance"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Counters a spell with nothing spliced onto it, reveals the hand, and leaves its cards alone")
    void countersWithoutSpliceLeavesHandAlone() {
        CrackTheEarth crackTheEarth = new CrackTheEarth();
        GoryosVengeance vengeance = new GoryosVengeance();
        harness.setHand(player1, List.of(crackTheEarth, vengeance));
        harness.addToBattlefield(player2, new IwamoriOfTheOpenFist());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, crackTheEarth.getId());

        harness.assertInGraveyard(player1, "Crack the Earth");
        harness.assertOnBattlefield(player2, "Iwamori of the Open Fist");
        assertThat(gameLogContains(player1.getUsername() + " reveals their hand")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(vengeance);
    }

    @Test
    @DisplayName("Discards only cards matching a spliced name, not other cards in hand")
    void leavesNonMatchingCardsInHand() {
        IwamoriOfTheOpenFist graveyardLegend = new IwamoriOfTheOpenFist();
        harness.setGraveyard(player1, List.of(graveyardLegend));
        CrackTheEarth crackTheEarth = new CrackTheEarth();
        CrackTheEarth nonMatchingCard = new CrackTheEarth();
        harness.setHand(player1, List.of(crackTheEarth, new GoryosVengeance(),
                new GoryosVengeance(), nonMatchingCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, graveyardLegend.getId(), List.of(1));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, crackTheEarth.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonMatchingCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Goryo's Vengeance"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Reveals the controller's hand even when the target spell cannot be countered")
    void revealsHandForUncounterableSpell() {
        IsaoEnlightenedBushi isao = new IsaoEnlightenedBushi();
        GoryosVengeance remainingCard = new GoryosVengeance();
        harness.setHand(player1, List.of(isao, remainingCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, isao.getId());

        assertThat(gameLogContains(player1.getUsername() + " reveals their hand")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingCard);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Isao, Enlightened Bushi");
    }

    @Test
    @DisplayName("Countering your own spliced spell does not enable opponent-caused discard replacements")
    void ownSpellDiscardDoesNotUseNephaliaAcademy() {
        IwamoriOfTheOpenFist graveyardLegend = new IwamoriOfTheOpenFist();
        CrackTheEarth host = new CrackTheEarth();
        GoryosVengeance splicedCard = new GoryosVengeance();
        harness.setGraveyard(player1, List.of(graveyardLegend));
        harness.addToBattlefield(player1, new NephaliaAcademy());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(host, splicedCard, new MinamosMeddling()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithSplice(player1, 0, graveyardLegend.getId(), List.of(1));
        harness.castAndResolveInstant(player1, 1, host.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(splicedCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding multiple matching cards is a single event for Inti")
    void matchingCardsAreDiscardedInOneEvent() {
        IwamoriOfTheOpenFist graveyardLegend = new IwamoriOfTheOpenFist();
        CrackTheEarth host = new CrackTheEarth();
        CrackTheEarth topCard = new CrackTheEarth();
        CrackTheEarth nextCard = new CrackTheEarth();
        harness.setGraveyard(player1, List.of(graveyardLegend));
        harness.addToBattlefield(player1, new IntiSeneschalOfTheSun());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(host, new GoryosVengeance(), new GoryosVengeance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, graveyardLegend.getId(), List.of(1));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, host.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }
}
