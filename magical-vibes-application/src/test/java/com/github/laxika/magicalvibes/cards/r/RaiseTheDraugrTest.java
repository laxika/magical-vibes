package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RaiseTheDraugrTest extends BaseCardTest {

    @Test
    @DisplayName("The single-card mode returns a creature card from the graveyard to hand")
    void returnsOneCreatureCard() {
        Card creature = new GrizzlyBears();
        Card spell = new RaiseTheDraugr();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        List<java.util.UUID> targets = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The two-card mode returns two creature cards that share a creature type")
    void returnsTwoCreaturesSharingType() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card wolf = new YoungWolf();
        Card spell = new RaiseTheDraugr();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, wolf));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(firstBear.getId(), secondBear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(wolf);
    }

    @Test
    @DisplayName("The shared-type mode cannot be cast without a legal creature pair")
    void requiresASharedTypePair() {
        Card bear = new GrizzlyBears();
        Card wolf = new YoungWolf();
        Card spell = new RaiseTheDraugr();
        harness.setGraveyard(player1, List.of(bear, wolf));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }
}
