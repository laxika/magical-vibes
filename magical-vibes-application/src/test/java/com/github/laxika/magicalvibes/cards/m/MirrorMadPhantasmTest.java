package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorMadPhantasmTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Mirror-Mad Phantasm has activated ability with correct effect")
    void hasCorrectActivatedAbility() {
        MirrorMadPhantasm card = new MirrorMadPhantasm();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect.class);

        ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect effect =
                (ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.cardName()).isEqualTo("Mirror-Mad Phantasm");
    }

    // ===== Resolving — finds copy in library =====

    @Test
    @DisplayName("Shuffles self into library and finds a Phantasm — revealed non-matching cards go to graveyard")
    void findsPhantasmAndMillsRevealedCards() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Library has only non-matching cards. The Phantasm gets shuffled in, so it will always
        // be found. Some non-matching cards revealed before it go to graveyard.
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // A Mirror-Mad Phantasm should be on the battlefield (the one shuffled in, found by name)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirror-Mad Phantasm"));

        // Total cards: 2 non-matching + 1 Phantasm shuffled in = 3 in library after shuffle
        // After reveal: Phantasm goes to battlefield, revealed non-matching go to graveyard,
        // remaining non-matching stay in library.
        // Cards in graveyard + cards in library should equal 2 (the original non-matching cards)
        int graveyardCount = (int) gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Llanowar Elves"))
                .count();
        int libraryCount = (int) gd.playerDecks.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Llanowar Elves"))
                .count();
        assertThat(graveyardCount + libraryCount).isEqualTo(2);
    }

    @Test
    @DisplayName("With another copy in library, finds a Phantasm and puts it onto battlefield")
    void findsAnotherCopyInLibrary() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Library has another Mirror-Mad Phantasm plus non-matching cards
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new MirrorMadPhantasm());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // A Mirror-Mad Phantasm should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirror-Mad Phantasm"));
    }

    // ===== Resolving — empty library =====

    @Test
    @DisplayName("Empty library — Phantasm shuffled in is immediately found and put onto battlefield")
    void emptyLibraryPhantasmFindsItself() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Empty library — only the shuffled Phantasm will be in it
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // The Phantasm should be back on the battlefield (shuffled in, found immediately)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirror-Mad Phantasm"));

        // Library and graveyard should be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mirror-Mad Phantasm"));
    }

    // ===== Source leaves battlefield before resolution =====

    @Test
    @DisplayName("If Mirror-Mad Phantasm leaves the battlefield before resolution, nothing happens")
    void leavingBattlefieldBeforeResolutionDoesNothing() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new MirrorMadPhantasm());

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

    // ===== Mana cost enforcement =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new MirrorMadPhantasm());
        harness.addMana(player1, ManaColor.BLUE, 1); // Need {1}{U}, only have {U}

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> harness.activateAbility(player1, 0, null, null));
    }
}
