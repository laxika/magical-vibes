package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorMadPhantasm.class, DarkthicketWolf.class, AvacynsPilgrim.class, CacklingCounterpart.class})
class MirrorMadPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles self into library and finds a Phantasm — revealed non-matching cards go to graveyard")
    void findsPhantasmAndMillsRevealedCards() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Library has only non-matching cards. The Phantasm gets shuffled in, so it will always
        // be found. Some non-matching cards revealed before it go to graveyard.
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new DarkthicketWolf(), new AvacynsPilgrim()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // A Mirror-Mad Phantasm should be on the battlefield (the one shuffled in, found by name)
        harness.assertOnBattlefield(player1, "Mirror-Mad Phantasm");

        // Total cards: 2 non-matching + 1 Phantasm shuffled in = 3 in library after shuffle
        // After reveal: Phantasm goes to battlefield, revealed non-matching go to graveyard,
        // remaining non-matching stay in library.
        // Cards in graveyard + cards in library should equal 2 (the original non-matching cards)
        int graveyardCount = (int) gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Darkthicket Wolf") || c.getName().equals("Avacyn's Pilgrim"))
                .count();
        int libraryCount = (int) gd.playerDecks.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Darkthicket Wolf") || c.getName().equals("Avacyn's Pilgrim"))
                .count();
        assertThat(graveyardCount + libraryCount).isEqualTo(2);
    }

    @Test
    @DisplayName("With another copy in library, finds a Phantasm and puts it onto battlefield")
    void findsAnotherCopyInLibrary() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Library has another Mirror-Mad Phantasm plus non-matching cards
        harness.setLibrary(player1, List.of(new DarkthicketWolf(), new MirrorMadPhantasm()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // A Mirror-Mad Phantasm should be on the battlefield
        harness.assertOnBattlefield(player1, "Mirror-Mad Phantasm");
    }

    @Test
    @DisplayName("Empty library — Phantasm shuffled in is immediately found and put onto battlefield")
    void emptyLibraryPhantasmFindsItself() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Empty library — only the shuffled Phantasm will be in it
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // The Phantasm should be back on the battlefield (shuffled in, found immediately)
        harness.assertOnBattlefield(player1, "Mirror-Mad Phantasm");

        // Library and graveyard should be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Mirror-Mad Phantasm");
    }

    @Test
    @DisplayName("If Mirror-Mad Phantasm leaves the battlefield before resolution, nothing happens")
    void leavingBattlefieldBeforeResolutionDoesNothing() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new DarkthicketWolf(), new MirrorMadPhantasm()));

        harness.activateAbility(player1, 0, null, null);

        // Remove the permanent before resolution (e.g. killed in response)
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        gd = harness.getGameData();

        // Library should be untouched
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        // No Mirror-Mad Phantasm on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 1); // Need {1}{U}, only have {U}

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> harness.activateAbility(player1, 0, null, null));
    }

    @Test
    @DisplayName("Token copy mills the whole library when no named card is present")
    void tokenCopyMillsWholeLibrary() {
        var original = harness.addToBattlefieldAndReturn(player1, new MirrorMadPhantasm());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.setLibrary(player1, List.of(new DarkthicketWolf(), new AvacynsPilgrim()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(original);
    }

    @Test
    @DisplayName("Exile replacement still allows reveal after starting the mandatory shuffle cost")
    void exileReplacementStillRevealsLibrary() {
        var phantasm = harness.addToBattlefieldAndReturn(player1, new MirrorMadPhantasm());
        phantasm.setExileIfLeavesBattlefield(true);
        harness.setLibrary(player1, List.of(new DarkthicketWolf(), new AvacynsPilgrim()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        harness.assertNotOnBattlefield(player1, "Mirror-Mad Phantasm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(phantasm.getOriginalCard());
    }

    @Test
    @DisplayName("Stolen Phantasm uses its owner's library and returns under its owner's control")
    void stolenPhantasmReturnsToOwner() {
        var phantasm = harness.addToBattlefieldAndReturn(player2, new MirrorMadPhantasm());
        gd.stolenCreatures.put(phantasm.getId(), player1.getId());
        harness.setLibrary(player1, List.of());
        var wolf = new DarkthicketWolf();
        harness.setLibrary(player2, List.of(wolf));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mirror-Mad Phantasm");
        harness.assertNotOnBattlefield(player2, "Mirror-Mad Phantasm");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(wolf);
    }
}
