package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExaltedSunborn;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalIntervention.class, ExaltedSunborn.class, Forest.class})
class TemporalInterventionTest extends BaseCardTest {

    @Test
    void choosesAndDiscardsNonlandCardFromOpponentsHand() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new ExaltedSunborn());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        ExaltedSunborn discarded = new ExaltedSunborn();
        harness.setHand(player2, List.of(discarded, new Forest()));
        harness.setHand(player1, List.of(new TemporalIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void reducesCostAfterNonlandPermanentLeavesBattlefield() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new ExaltedSunborn());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        harness.setHand(player2, List.of(new ExaltedSunborn()));
        harness.setHand(player1, List.of(new TemporalIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
    }

    @Test
    void reducesCostAfterSpellIsWarped() {
        harness.setHand(player1, List.of(new ExaltedSunborn(), new TemporalIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new ExaltedSunborn()));
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
    }

    @Test
    void cannotUseReducedCostWithoutVoidEvent() {
        harness.setHand(player2, List.of(new ExaltedSunborn()));
        harness.setHand(player1, List.of(new TemporalIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landLeavingBattlefieldDoesNotEnableVoid() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        harness.setHand(player2, List.of(new ExaltedSunborn()));
        harness.setHand(player1, List.of(new TemporalIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysFullCostWithoutVoidAndDiscardsExactlyChosenCard() {
        ExaltedSunborn unchosen = new ExaltedSunborn();
        TemporalIntervention chosen = new TemporalIntervention();
        harness.setHand(player2, List.of(unchosen, chosen, new Forest()));
        harness.setHand(player1, List.of(new TemporalIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).contains(unchosen).doesNotContain(chosen).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosen);
        harness.assertInGraveyard(player1, "Temporal Intervention");
    }

    @Test
    void resolvesWithoutDiscardWhenOpponentHasOnlyLands() {
        Forest land = new Forest();
        harness.setHand(player2, List.of(land));
        harness.setHand(player1, List.of(new TemporalIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Temporal Intervention");
    }

    @Test
    void resolvesWithoutChoiceWhenOpponentsHandIsEmpty() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TemporalIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Temporal Intervention");
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new TemporalIntervention(), new ExaltedSunborn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
