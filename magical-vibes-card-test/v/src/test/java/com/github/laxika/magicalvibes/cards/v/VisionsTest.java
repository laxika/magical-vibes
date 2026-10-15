package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AkronLegionnaire;
import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.t.TundraWolves;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Visions.class, AkronLegionnaire.class, DivineOffering.class, HolyDay.class,
        TundraWolves.class, ZephyrFalcon.class, DurkwoodBoars.class})
class VisionsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving asks the controller whether to shuffle the target's library")
    void resolvingAsksControllerWhetherToShuffle() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The shuffle prompt names exactly the top five cards of the target's library")
    void namesExactlyTopFiveCards() {
        List<Card> library = List.of(
                new AkronLegionnaire(),
                new DivineOffering(),
                new HolyDay(),
                new TundraWolves(),
                new ZephyrFalcon(),
                new DurkwoodBoars());
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.description())
                .contains("Top 5 cards", "Akron Legionnaire", "Divine Offering", "Holy Day",
                        "Tundra Wolves", "Zephyr Falcon")
                .doesNotContain("Durkwood Boars");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Declining leaves the target's library untouched and in the same order")
    void decliningLeavesTargetLibraryUnchanged() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> before = new ArrayList<>(gd.playerDecks.get(player2.getId()));

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        // Pure look: no reorder, no shuffle — the whole library keeps its exact order.
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(before);
    }

    @Test
    @DisplayName("Accepting shuffles the target's library without removing any cards")
    void acceptingShufflesTargetLibrary() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> before = new ArrayList<>(gd.playerDecks.get(player2.getId()));

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        // The shuffle only randomizes the library; no cards are drawn, exiled, or milled.
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrderElementsOf(before);
    }

    @Test
    @DisplayName("An empty target library still offers the shuffle choice")
    void emptyTargetLibraryStillOffersChoice() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player2, List.of());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Visions can look at the caster's own library")
    void canTargetOwnLibrary() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A library with fewer than five cards shows all available cards without moving them")
    void shortLibraryShowsAllAvailableCards() {
        List<Card> library = List.of(new HolyDay(), new TundraWolves());
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
        assertThat(may.description()).contains("Top 2 cards", "Holy Day", "Tundra Wolves");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Accepting the shuffle completes it during resolution without a separate stack ability")
    void acceptingShuffleDoesNotCreateSeparateAbility() {
        harness.setHand(player1, List.of(new Visions(), new HolyDay()));
        harness.setHand(player2, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
    }
}
