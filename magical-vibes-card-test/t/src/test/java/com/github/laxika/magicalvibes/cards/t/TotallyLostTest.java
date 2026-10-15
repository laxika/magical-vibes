package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TotallyLost.class, GrizzlyBears.class, Forest.class, MillennialGargoyle.class, PropheticPrism.class})
class TotallyLostTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target nonland permanent on top of its owner's library")
    void putsTargetNonlandPermanentOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TotallyLost()));
        addMana();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        List<Card> deck = gameData.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        // A legal target has to exist or the spell could not be cast at all (CR 601.2c).
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TotallyLost()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetLeavesBattlefieldBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TotallyLost()));
        addMana();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can target a noncreature artifact and preserves the rest of the library order")
    void putsNoncreatureArtifactOnTopWithoutShuffling() {
        Card prism = new PropheticPrism();
        Card first = new MillennialGargoyle();
        Card second = new TotallyLost();
        harness.addToBattlefield(player2, prism);
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new TotallyLost()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Prophetic Prism"));

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(prism, first, second);
        harness.assertInGraveyard(player1, "Totally Lost");
    }

    @Test
    @DisplayName("Can put a permanent its caster controls on top of that player's library")
    void canTargetOwnPermanent() {
        Card gargoyle = new MillennialGargoyle();
        harness.addToBattlefield(player1, gargoyle);
        harness.setHand(player1, List.of(new TotallyLost()));
        addMana();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Millennial Gargoyle"));

        harness.assertNotOnBattlefield(player1, "Millennial Gargoyle");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(gargoyle);
    }

    @Test
    @DisplayName("Uses the owner's library when another player controls the permanent")
    void putsBorrowedPermanentInOwnersLibrary() {
        Card gargoyle = new MillennialGargoyle();
        gargoyle.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, gargoyle);
        harness.setHand(player1, List.of(new TotallyLost()));
        addMana();
        int ownerDeckSize = gd.playerDecks.get(player1.getId()).size();
        List<Card> controllerDeck = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Millennial Gargoyle"));

        harness.assertNotOnBattlefield(player2, "Millennial Gargoyle");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownerDeckSize + 1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(gargoyle);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(controllerDeck);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
