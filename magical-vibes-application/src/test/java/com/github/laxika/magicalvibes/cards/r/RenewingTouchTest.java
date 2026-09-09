package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SleightOfHand;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenewingTouch.class, RiverBear.class, SleightOfHand.class})
class RenewingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Only creature cards in the graveyard are valid targets")
    void onlyCreatureCardsAreValidTargets() {
        RiverBear firstCreature = new RiverBear();
        SleightOfHand nonCreature = new SleightOfHand();
        RiverBear secondCreature = new RiverBear();
        harness.setGraveyard(player1, List.of(firstCreature, nonCreature, secondCreature));
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstCreature.getId(), secondCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selecting creature cards shuffles them from graveyard into library")
    void selectingCreaturesShufflesIntoLibrary() {
        RiverBear firstCreature = new RiverBear();
        RiverBear secondCreature = new RiverBear();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        List<UUID> validIds = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        harness.handleMultipleCardsChosen(player1, validIds);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .contains(firstCreature.getId(), secondCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(firstCreature.getId(), secondCreature.getId());
        harness.assertInGraveyard(player1, "Renewing Touch");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2);
    }

    @Test
    @DisplayName("Selecting a subset of creature cards leaves the rest in the graveyard")
    void selectingSubsetLeavesRest() {
        RiverBear firstCreature = new RiverBear();
        RiverBear secondCreature = new RiverBear();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .contains(firstCreature.getId())
                .hasSize(libSizeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(secondCreature.getId())
                .doesNotContain(firstCreature.getId());
        harness.assertInGraveyard(player1, "Renewing Touch");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Selecting zero targets leaves the graveyard unchanged")
    void selectingZeroTargetsLeavesGraveyardUnchanged() {
        RiverBear creature = new RiverBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(creature.getId());
        harness.assertInGraveyard(player1, "Renewing Touch");
    }

    @Test
    @DisplayName("Only your graveyard is searched")
    void onlyControllerGraveyardIsSearched() {
        RiverBear ownCreature = new RiverBear();
        RiverBear opponentCreature = new RiverBear();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .contains(ownCreature.getId())
                .hasSize(libSizeBefore + 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opponentCreature.getId());
        harness.assertInGraveyard(player1, "Renewing Touch");
    }

    @Test
    @DisplayName("Casting with no creature cards in graveyard puts spell on stack directly")
    void noCreatureCardsPutsOnStack() {
        SleightOfHand nonCreature = new SleightOfHand();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new RenewingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(nonCreature.getId());
        harness.assertInGraveyard(player1, "Renewing Touch");
    }
}
