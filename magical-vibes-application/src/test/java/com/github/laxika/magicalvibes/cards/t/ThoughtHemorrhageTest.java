package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Deals 3 damage per revealed copy and exiles them")
    void dealsThreeDamagePerCopyAndExiles() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card peek = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(bears1, bears2, peek)));

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No selection step — the exile is mandatory and automatic.
        harness.handleListChoice(player1, "Grizzly Bears");

        // 2 copies revealed from hand -> 6 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        // Both copies exiled, Peek untouched.
        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count();
        assertThat(exiled).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();
    }

    @Test
    @DisplayName("Exiles all copies from hand, graveyard, and library automatically")
    void exilesFromAllZones() {
        Card handBears = new GrizzlyBears();
        Card graveBears = new GrizzlyBears();
        Card libraryBears = new GrizzlyBears();

        harness.setHand(player2, new ArrayList<>(List.of(handBears)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveBears)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(libraryBears);

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count();
        assertThat(exiled).isEqualTo(3);
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Damage counts only copies revealed from hand, not other zones")
    void damageCountsOnlyHandCopies() {
        Card handBears = new GrizzlyBears();
        Card graveBears1 = new GrizzlyBears();
        Card graveBears2 = new GrizzlyBears();

        harness.setHand(player2, new ArrayList<>(List.of(handBears)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveBears1, graveBears2)));

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        // Only the single hand copy deals damage -> 3, even though 3 copies are exiled.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count();
        assertThat(exiled).isEqualTo(3);
    }

    @Test
    @DisplayName("Naming a card with no copies deals no damage, exiles nothing, and shuffles")
    void noCopiesNoDamage() {
        Card peek = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(peek)));
        harness.setGraveyard(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ThoughtHemorrhage()));
        giveMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

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

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thought Hemorrhage"));
    }
}
