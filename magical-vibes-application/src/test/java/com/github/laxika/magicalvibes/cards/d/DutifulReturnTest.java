package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DutifulReturnTest extends BaseCardTest {

    @Test
    void castingWithCreatureCardsPromptsForUpToTwoTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.maxCount()).isEqualTo(2);
        assertThat(interaction.validCardIds()).hasSize(2);
    }

    @Test
    void choosingTwoCreatureCardsReturnsBothToHand() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void choosingOneCreatureCardLeavesTheOtherInGraveyard() {
        Card creature = new GrizzlyBears();
        Card otherCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new LeoninScimitar()));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void noCreatureCardsSkipTargetPrompt() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
