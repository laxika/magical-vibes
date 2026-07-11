package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

class RenewingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Only creature cards in the graveyard are valid targets")
    void onlyCreatureCardsAreValidTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        // Prompted to select creature cards only — Lightning Bolt (sorcery/instant) excluded
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
        // "Any number" — cap is the number of matching creature cards
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selecting creature cards shuffles them from graveyard into library")
    void selectingCreaturesShufflesIntoLibrary() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        // Creatures gone from graveyard; only Renewing Touch itself remains
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Renewing Touch"));
        // Library gained the two creatures
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2);
    }

    @Test
    @DisplayName("Selecting a subset of creature cards leaves the rest in the graveyard")
    void selectingSubsetLeavesRest() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        // One creature + Renewing Touch left in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 1);
    }

    @Test
    @DisplayName("Casting with no creature cards in graveyard puts spell on stack directly")
    void noCreatureCardsPutsOnStack() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player1.getId());

        // No valid creature targets — no prompt, spell goes straight to the stack
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Renewing Touch"));
    }
}
