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
}
