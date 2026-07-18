package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ClarionUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Clarion Ultimatum prompts the controller to choose up to five permanents")
    void promptsPermanentChoice() {
        List<Permanent> forests = setupForests(3);
        setupLibrary();
        castClarion();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrderElementsOf(forests.stream().map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("The choice is capped at five permanents even with more on the battlefield")
    void choiceCappedAtFive() {
        setupForests(6);
        setupLibrary();
        castClarion();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(5);
    }

    @Test
    @DisplayName("Each chosen permanent searches the library for a same-named card put onto the battlefield tapped")
    void fetchesSameNamedCardsTapped() {
        List<Permanent> forests = setupForests(2);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1,
                List.of(forests.get(0).getId(), forests.get(1).getId()));

        // First same-name search is offered; only Forest cards are legal.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> "Forest".equals(c.getName()));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        // Second permanent (also a Forest) offers its own search.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Two original Forests plus two fetched Forests.
        assertThat(forestsOnBattlefield()).hasSize(4);
        long tapped = forestsOnBattlefield().stream().filter(Permanent::isTapped).count();
        assertThat(tapped).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("The search for each chosen permanent is optional")
    void mayDeclineEachSearch() {
        List<Permanent> forests = setupForests(1);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(forests.get(0).getId()));

        // Decline the only same-name search.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Nothing fetched — only the original Forest remains.
        assertThat(forestsOnBattlefield()).hasSize(1);
    }

    @Test
    @DisplayName("Choosing no permanents runs no search")
    void chooseNoneNoSearch() {
        setupForests(3);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(forestsOnBattlefield()).hasSize(3);
    }

    // ===== Helpers =====

    private List<Permanent> setupForests(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Forest()))
                .toList();
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Forest(), new Island(), new GrizzlyBears()));
    }

    private void castClarion() {
        harness.setHand(player1, List.of(new ClarionUltimatum()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private List<Permanent> forestsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Forest".equals(p.getCard().getName()))
                .toList();
    }
}
