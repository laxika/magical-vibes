package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ClingingAnemones;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SereneRemembrance.class, Slaughterhorn.class, ClingingAnemones.class,
        LeylineOfSanctity.class, CosisTrickster.class})
class SereneRemembranceTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles up to three targeted cards from the targeted graveyard into its owner's library")
    void shufflesTargetedCardsIntoLibrary() {
        harness.setGraveyard(player1, List.of(new Slaughterhorn(), new ClingingAnemones()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(validIds).hasSize(2);
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Slaughterhorn");
        harness.assertNotInGraveyard(player1, "Clinging Anemones");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 3); // 2 targets + Serene Remembrance
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library instead of going to the graveyard")
    void shufflesItselfIntoLibrary() {
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertNotInGraveyard(player1, "Serene Remembrance");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getName().equals("Serene Remembrance"));
    }

    @Test
    @DisplayName("Can target an opponent's graveyard")
    void canTargetOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new Slaughterhorn(), new ClingingAnemones()));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);
        // The spell itself goes to its own owner's library, not the targeted player's
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getName().equals("Serene Remembrance"));
    }

    @Test
    void canChooseZeroCardsFromNonemptyGraveyard() {
        Card unselected = new Slaughterhorn();
        harness.setGraveyard(player1, List.of(unselected));
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Serene Remembrance"));
    }

    @Test
    void doesNotResolveWhenEveryCardTargetLeavesGraveyard() {
        Card target = new Slaughterhorn();
        SereneRemembrance spell = new SereneRemembrance();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void resolvesForRemainingLegalCardTargets() {
        Card removed = new Slaughterhorn();
        Card remaining = new ClingingAnemones();
        SereneRemembrance spell = new SereneRemembrance();
        harness.setGraveyard(player2, List.of(removed, remaining));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setExile(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).contains(remaining).doesNotContain(removed);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(spell);
    }

    @Test
    @CardUsed({LeylineOfSanctity.class})
    void canChooseCardsFromHexproofOpponentsGraveyard() {
        Card target = new Slaughterhorn();
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({CosisTrickster.class})
    void shufflesOwnLibraryOnlyOnceWithOwnGraveyardTargets() {
        Card target = new Slaughterhorn();
        harness.addToBattlefield(player2, new CosisTrickster());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CosisTrickster.class);
    }

    @Test
    @CardUsed({CosisTrickster.class})
    void doesNotShuffleOpponentLibraryWhenNoCardsAreChosen() {
        harness.addToBattlefield(player1, new CosisTrickster());
        harness.setGraveyard(player2, List.of(new Slaughterhorn()));
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Serene Remembrance"));
    }

    @Test
    @DisplayName("Selection is capped at three cards")
    void selectionCappedAtThree() {
        Card extra = new Slaughterhorn();
        harness.setGraveyard(player1, List.of(new Slaughterhorn(), new ClingingAnemones(), extra, new ClingingAnemones()));
        harness.setHand(player1, List.of(new SereneRemembrance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(4);
    }
}
