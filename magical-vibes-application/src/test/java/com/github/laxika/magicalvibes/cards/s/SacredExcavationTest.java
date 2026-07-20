package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.CompellingArgument;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class SacredExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with two cycling cards in graveyard prompts for target selection")
    void castingWithCyclingCardsPromptsTargetSelection() {
        harness.setGraveyard(player1, List.of(new Censor(), new CompellingArgument()));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selecting two cycling cards returns them to hand on resolution")
    void selectingTwoTargetsReturnsToHand() {
        harness.setGraveyard(player1, List.of(new Censor(), new CompellingArgument()));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Censor"))
                .anyMatch(c -> c.getName().equals("Compelling Argument"));
        // Sacred Excavation is the only card left in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Sacred Excavation"));
    }

    @Test
    @DisplayName("Only cards with cycling are valid targets")
    void onlyCyclingCardsAreValidTargets() {
        Card cycling = new Censor();
        Card noCycling = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(cycling, noCycling));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(cycling.getId());
    }

    @Test
    @DisplayName("Casting with no cycling cards in graveyard skips target prompt")
    void castingWithNoCyclingCardsSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Non-cycling card untouched; Sacred Excavation also goes to graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Sacred Excavation"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
