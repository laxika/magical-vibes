package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.y.YavimayaScion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfPossibility.class, SerraAngel.class, AvatarOfMight.class,
        GrizzlyBears.class, YavimayaScion.class, NuisanceEngine.class, Unsummon.class})
class PriestOfPossibilityTest extends BaseCardTest {

    @Test
    void gainsKeywordsAndProtectionFromTheTopSeven() {
        harness.setLibrary(player1, List.of(
                new SerraAngel(),
                new AvatarOfMight(),
                new YavimayaScion(),
                new GrizzlyBears()));

        Permanent priest = harness.enterBattlefieldAndReturn(player1, new PriestOfPossibility());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, priest, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.TRAMPLE)).isTrue();

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NuisanceEngine());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, priest, artifact)).isTrue();
    }

    @Test
    void onlyChecksTheTopSevenCards() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new SerraAngel()));

        Permanent priest = harness.enterBattlefieldAndReturn(player1, new PriestOfPossibility());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, priest, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void retainsKeywordsAndProtectionAfterReturningToHandAndBeingCastAgain() {
        PriestOfPossibility card = new PriestOfPossibility();
        harness.setLibrary(player1, List.of(new SerraAngel(), new YavimayaScion()));
        Permanent priest = harness.enterBattlefieldAndReturn(player1, card);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, priest.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(card);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, card, "{1}{W}");
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Priest of Possibility");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.VIGILANCE)).isTrue();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NuisanceEngine());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, returned, artifact)).isTrue();
    }

    @Test
    void showsOnlyTheControllerTheTopSevenCardsBeforeShuffling() {
        List<Card> cards = List.of(
                new SerraAngel(), new AvatarOfMight(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new YavimayaScion());
        harness.setLibrary(player1, cards);
        harness.clearMessages();

        harness.enterBattlefieldAndReturn(player1, new PriestOfPossibility());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .singleElement().satisfies(message -> assertThat(message)
                        .contains("Serra Angel", "Avatar of Might", "Grizzly Bears")
                        .doesNotContain("Yavimaya Scion"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
    }

    @Test
    void emptyLibraryDoesNotGrantAbilitiesOrRemoveThePriest() {
        harness.setLibrary(player1, List.of());

        Permanent priest = harness.enterBattlefieldAndReturn(player1, new PriestOfPossibility());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Priest of Possibility");
        assertThat(gqs.hasKeyword(gd, priest, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void checksItsControllersLibraryRatherThanTheOpponents() {
        harness.setLibrary(player1, List.of(new SerraAngel()));
        harness.setLibrary(player2, List.of(new AvatarOfMight()));

        Permanent priest = harness.enterBattlefieldAndReturn(player2, new PriestOfPossibility());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, priest, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.VIGILANCE)).isFalse();
    }
}
