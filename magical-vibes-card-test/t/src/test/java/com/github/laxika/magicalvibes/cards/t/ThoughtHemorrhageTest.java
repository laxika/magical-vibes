package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlitzHellion;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtHemorrhage.class, BlitzHellion.class, Terminate.class, Swamp.class})
class ThoughtHemorrhageTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving prompts the caster for a card name choice")
    void resolvingPromptsForCardNameChoice() {
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Deals 3 damage per revealed copy and exiles them")
    void dealsThreeDamagePerCopyAndExiles() {
        Card hellion1 = new BlitzHellion();
        Card hellion2 = new BlitzHellion();
        Card terminate = new Terminate();
        harness.setHand(player2, List.of(hellion1, hellion2, terminate));

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Blitz Hellion");
        harness.handleMultipleCardsChosen(player1, List.of(hellion1.getId(), hellion2.getId()));

        // 2 copies revealed from hand -> 6 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        // Both copies exiled, Terminate untouched.
        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Blitz Hellion")).count();
        assertThat(exiled).isEqualTo(2);
        harness.assertNotInHand(player2, "Blitz Hellion");
        harness.assertInHand(player2, "Terminate");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();
    }

    @Test
    @DisplayName("Exiles all graveyard copies and selected hand and library copies")
    void exilesFromAllZones() {
        Card handHellion = new BlitzHellion();
        Card graveHellion = new BlitzHellion();
        Card libraryHellion = new BlitzHellion();

        harness.setHand(player2, List.of(handHellion));
        harness.setGraveyard(player2, List.of(graveHellion));
        harness.setLibrary(player2, List.of(libraryHellion));

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Blitz Hellion");
        harness.handleMultipleCardsChosen(player1, List.of(handHellion.getId(), libraryHellion.getId()));

        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Blitz Hellion")).count();
        assertThat(exiled).isEqualTo(3);
        harness.assertNotInHand(player2, "Blitz Hellion");
        harness.assertNotInGraveyard(player2, "Blitz Hellion");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Blitz Hellion"));
    }

    @Test
    @DisplayName("Damage counts only copies revealed from hand, not other zones")
    void damageCountsOnlyHandCopies() {
        Card handHellion = new BlitzHellion();
        Card graveHellion1 = new BlitzHellion();
        Card graveHellion2 = new BlitzHellion();

        harness.setHand(player2, List.of(handHellion));
        harness.setGraveyard(player2, List.of(graveHellion1, graveHellion2));

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Blitz Hellion");
        harness.handleMultipleCardsChosen(player1, List.of(handHellion.getId()));

        // Only the single hand copy deals damage -> 3, even though 3 copies are exiled.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Blitz Hellion")).count();
        assertThat(exiled).isEqualTo(3);
    }

    @Test
    @DisplayName("Naming a card with no copies deals no damage, exiles nothing, and shuffles")
    void noCopiesNoDamage() {
        Card terminate = new Terminate();
        harness.setHand(player2, List.of(terminate));
        harness.setGraveyard(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Blitz Hellion");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("exiles 0 cards"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Resolves fully and goes to the caster's graveyard")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Blitz Hellion");

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Thought Hemorrhage");
    }

    @Test
    @DisplayName("May leave revealed hand and library copies while graveyard copies must be exiled")
    void mayLeaveHiddenZoneCopies() {
        Card handCopy = new BlitzHellion();
        Card graveyardCopy = new BlitzHellion();
        Card libraryCopy = new BlitzHellion();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Blitz Hellion");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCopy);
        harness.assertNotInGraveyard(player2, "Blitz Hellion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a land card name without dealing damage or exiling cards")
    void cannotNameLand() {
        Card swamp = new Swamp();
        harness.setHand(player2, List.of(swamp));
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Swamp"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(swamp);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a name that is not a real card name")
    void cannotNameNonexistentCard() {
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleListChoice(player1, "This Is Not A Real Magic Card Name"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("May exile only some matching hidden-zone cards")
    void mayChooseOnlySomeCopies() {
        Card handCopy = new BlitzHellion();
        Card firstLibraryCopy = new BlitzHellion();
        Card secondLibraryCopy = new BlitzHellion();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(firstLibraryCopy, secondLibraryCopy));
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Blitz Hellion");
        harness.handleMultipleCardsChosen(player1, List.of(firstLibraryCopy.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondLibraryCopy);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstLibraryCopy);
    }

    @Test
    @DisplayName("May target the caster and must exile matching graveyard copies with an empty hand")
    void mayTargetSelfWithNoHandCopies() {
        Card graveyardCopy = new BlitzHellion();
        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        harness.setGraveyard(player1, List.of(graveyardCopy));
        harness.setLibrary(player1, List.of(new Terminate()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleListChoice(player1, "Blitz Hellion");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCopy);
        harness.assertNotInGraveyard(player1, "Blitz Hellion");
        harness.assertInGraveyard(player1, "Thought Hemorrhage");
        assertThat(gd.stack).isEmpty();
    }
}
