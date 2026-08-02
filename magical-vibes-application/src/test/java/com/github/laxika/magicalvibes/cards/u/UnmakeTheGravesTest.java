package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnmakeTheGravesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two chosen creature cards from the graveyard to hand")
    void returnsTwoChosenCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(validIds).hasSize(2);
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Unmake the Graves");
    }

    @Test
    @DisplayName("Up to two: choosing a single creature returns only that one")
    void choosingOneCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Only creature cards are legal targets")
    void onlyCreatureCardsAreLegalTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new LeoninScimitar()));
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Casting with no creature cards in the graveyard skips the prompt and resolves")
    void noCreaturesInGraveyard() {
        harness.setHand(player1, List.of(new UnmakeTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unmake the Graves");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
