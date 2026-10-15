package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.CompellingArgument;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacredExcavation.class, Censor.class, CompellingArgument.class, DuneBeetle.class})
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

        harness.assertInHand(player1, "Censor");
        harness.assertInHand(player1, "Compelling Argument");
        // Sacred Excavation is the only card left in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Sacred Excavation"));
    }

    @Test
    @DisplayName("Only cards with cycling are valid targets")
    void onlyCyclingCardsAreValidTargets() {
        Card cycling = new Censor();
        Card noCycling = new DuneBeetle();
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
        harness.setGraveyard(player1, List.of(new DuneBeetle()));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Non-cycling card untouched; Sacred Excavation also goes to graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .anyMatch(c -> c.getName().equals("Dune Beetle"))
                .anyMatch(c -> c.getName().equals("Sacred Excavation"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canChooseZeroTargetsEvenWhenCyclingCardsAreAvailable() {
        Card cycling = new Censor();
        harness.setGraveyard(player1, List.of(cycling));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cycling).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseOnlyOneOfTwoAvailableTargets() {
        Card chosen = new Censor();
        Card unchosen = new CompellingArgument();
        harness.setGraveyard(player1, List.of(chosen, unchosen));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
    }

    @Test
    void cannotChooseCyclingCardsFromOpponentsGraveyard() {
        Card ownCard = new Censor();
        Card opponentCard = new CompellingArgument();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void returnsRemainingLegalTargetWhenOtherTargetLeavesGraveyard() {
        Card removed = new Censor();
        Card remaining = new CompellingArgument();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        harness.assertInGraveyard(player1, "Sacred Excavation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsNothingWhenAllTargetsLeaveGraveyard() {
        Card target = new Censor();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sacred Excavation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseMoreThanTwoTargetsOrChooseSameCardTwice() {
        Card first = new Censor();
        Card second = new CompellingArgument();
        Card third = new Censor();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new SacredExcavation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third);
    }
}
