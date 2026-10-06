package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JuniperOrderAdvocate;
import com.github.laxika.magicalvibes.cards.k.KjeldoranEscort;
import com.github.laxika.magicalvibes.cards.k.KjeldoranHomeGuard;
import com.github.laxika.magicalvibes.cards.n.NobleSteeds;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Reinforcements.class, KjeldoranHomeGuard.class, KjeldoranEscort.class,
        JuniperOrderAdvocate.class, NobleSteeds.class})
class ReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("At most three creature cards may be chosen")
    void choiceIsCappedAtThree() {
        harness.setGraveyard(player1, List.of(new KjeldoranHomeGuard(), new KjeldoranHomeGuard(),
                new KjeldoranEscort(), new KjeldoranEscort()));
        harness.castFromHand(player1, new Reinforcements(), "{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Fewer creature cards than three caps the choice at what is available")
    void choiceIsCappedAtAvailableCards() {
        harness.setGraveyard(player1, List.of(new KjeldoranHomeGuard(), new KjeldoranEscort()));
        harness.castFromHand(player1, new Reinforcements(), "{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen creature cards move from the graveyard to the top of the library")
    void chosenCreaturesGoOnTopOfLibrary() {
        Card homeGuard = new KjeldoranHomeGuard();
        Card escort = new KjeldoranEscort();
        Reinforcements spell = new Reinforcements();
        harness.setGraveyard(player1, List.of(homeGuard, escort));

        harness.castFromHand(player1, spell, "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(homeGuard.getId(), escort.getId()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(homeGuard.getId(), escort.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(spell.getId());
    }

    @Test
    @DisplayName("Only creature cards in your own graveyard are legal targets")
    void onlyOwnCreatureCardsAreLegalTargets() {
        Card homeGuard = new KjeldoranHomeGuard();
        harness.setGraveyard(player1, List.of(homeGuard, new NobleSteeds()));
        harness.setGraveyard(player2, List.of(new KjeldoranEscort()));

        harness.castFromHand(player1, new Reinforcements(), "{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(homeGuard.getId());
    }

    @Test
    @DisplayName("With no creature cards in the graveyard the spell resolves doing nothing")
    void noCreatureCardsResolvesWithNoEffect() {
        Card nonCreature = new NobleSteeds();
        Card topCard = new JuniperOrderAdvocate();
        Reinforcements spell = new Reinforcements();
        harness.setGraveyard(player1, List.of(nonCreature));
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        harness.castFromHand(player1, spell, "{W}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(nonCreature.getId(), spell.getId());
    }

    @Test
    @DisplayName("Choosing no targets leaves available creature cards in the graveyard")
    void choosingNoTargetsLeavesAvailableCreatures() {
        Card creature = new KjeldoranHomeGuard();
        Reinforcements spell = new Reinforcements();
        harness.setGraveyard(player1, List.of(creature));

        harness.castFromHand(player1, spell, "{W}");
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId(), spell.getId());
    }

    @Test
    @DisplayName("The controller chooses the order of multiple cards when the spell resolves")
    void controllerChoosesOrderWhenMultipleCardsResolve() {
        Card homeGuard = new KjeldoranHomeGuard();
        Card escort = new KjeldoranEscort();
        harness.setGraveyard(player1, List.of(homeGuard, escort));

        harness.castFromHand(player1, new Reinforcements(), "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(homeGuard.getId(), escort.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).extracting(Card::getId)
                .containsExactly(homeGuard.getId(), escort.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .extracting(Card::getId)
                .containsExactly(escort.getId(), homeGuard.getId());
    }

    @Test
    @DisplayName("Three chosen creatures are ordered above the existing library and unchosen cards stay behind")
    void threeTargetsArePlacedAboveExistingLibrary() {
        Card homeGuard = new KjeldoranHomeGuard();
        Card escort = new KjeldoranEscort();
        Card advocate = new JuniperOrderAdvocate();
        Card unchosen = new KjeldoranEscort();
        Card originalTop = new NobleSteeds();
        Reinforcements spell = new Reinforcements();
        harness.setGraveyard(player1, List.of(homeGuard, escort, advocate, unchosen));
        harness.setLibrary(player1, List.of(originalTop));

        harness.castFromHand(player1, spell, "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(homeGuard.getId(), escort.getId(), advocate.getId()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(advocate.getId(), homeGuard.getId(), escort.getId(), originalTop.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(unchosen.getId(), spell.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("One chosen creature is put on top without an ordering prompt")
    void singleTargetGoesOnTopWithoutReordering() {
        Card creature = new KjeldoranHomeGuard();
        Card unchosen = new KjeldoranEscort();
        Card originalTop = new NobleSteeds();
        harness.setGraveyard(player1, List.of(creature, unchosen));
        harness.setLibrary(player1, List.of(originalTop));

        harness.castFromHand(player1, new Reinforcements(), "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId(), originalTop.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(unchosen.getId()).doesNotContain(creature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target moved by a response is skipped while the remaining target is returned")
    void remainingLegalTargetStillReturns() {
        Card homeGuard = new KjeldoranHomeGuard();
        Card escort = new KjeldoranEscort();
        Card originalTop = new NobleSteeds();
        Reinforcements spell = new Reinforcements();
        Reinforcements response = new Reinforcements();
        harness.setGraveyard(player1, List.of(homeGuard, escort));
        harness.setLibrary(player1, List.of(originalTop));

        harness.castFromHand(player1, spell, "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(homeGuard.getId(), escort.getId()));
        harness.castFromHand(player1, response, "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(homeGuard.getId()));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(escort.getId(), homeGuard.getId(), originalTop.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(response.getId(), spell.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When its only target leaves the graveyard the spell does not move that card again")
    void allTargetsLeavingGraveyardStopsResolution() {
        Card creature = new KjeldoranHomeGuard();
        Card originalTop = new NobleSteeds();
        Reinforcements spell = new Reinforcements();
        Reinforcements response = new Reinforcements();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(originalTop));

        harness.castFromHand(player1, spell, "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.castFromHand(player1, response, "{W}");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId(), originalTop.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(response.getId(), spell.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
