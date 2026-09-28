package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyFate;
import com.github.laxika.magicalvibes.cards.g.GuidedStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyFate.class, GuidedStrike.class, KrosanReclamation.class})
class KrosanReclamationTest extends BaseCardTest {

    @Test
    void shufflesUpToTwoCardsFromTargetPlayersGraveyard() {
        KrosanReclamation spell = new KrosanReclamation();
        harness.setGraveyard(player2, List.of(
                new KrosanReclamation(), new KrosanReclamation(), new KrosanReclamation()));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);

        List<UUID> targetIds = new ArrayList<>(choice.validCardIds());
        harness.handleMultipleCardsChosen(player1, targetIds.subList(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void canChooseOneCardFromTargetPlayersGraveyard() {
        KrosanReclamation firstCard = new KrosanReclamation();
        KrosanReclamation secondCard = new KrosanReclamation();
        KrosanReclamation spell = new KrosanReclamation();
        harness.setGraveyard(player1, List.of(firstCard, secondCard));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0, player1.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstCard.getId(), secondCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(secondCard, spell)
                .doesNotContain(firstCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 1);
    }

    @Test
    void canChooseZeroCardsFromTargetPlayersGraveyard() {
        KrosanReclamation graveyardCard = new KrosanReclamation();
        KrosanReclamation spell = new KrosanReclamation();
        harness.setGraveyard(player2, List.of(graveyardCard));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void flashbackExilesSpellAfterResolution() {
        KrosanReclamation spell = new KrosanReclamation();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(new KrosanReclamation()));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        addMana();

        harness.castFlashback(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void flashbackWithNoCardsInTargetPlayersGraveyardStillExilesSpell() {
        KrosanReclamation spell = new KrosanReclamation();
        harness.setGraveyard(player1, List.of(spell));
        addMana();

        harness.castFlashback(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    void canChooseFewerThanTwoCards() {
        KrosanReclamation spell = new KrosanReclamation();
        GrizzlyFate selected = new GrizzlyFate();
        GuidedStrike remaining = new GuidedStrike();
        harness.setGraveyard(player2, List.of(selected, remaining));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 1);
    }

    @Test
    void canChooseNoCards() {
        KrosanReclamation spell = new KrosanReclamation();
        GrizzlyFate remaining = new GrizzlyFate();
        harness.setGraveyard(player2, List.of(remaining));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void targetingEmptyGraveyardNeedsNoCardChoice() {
        KrosanReclamation spell = new KrosanReclamation();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }
}
