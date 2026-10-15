package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinkIntoTakenuma.class, Swamp.class, Island.class, GrizzlyBears.class})
class SinkIntoTakenumaTest extends BaseCardTest {

    @Test
    @DisplayName("Returns chosen Swamps and makes the target player discard that many cards")
    void returnsChosenSwampsAndMakesTargetDiscard() {
        Permanent firstSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent secondSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        castAtPlayer2();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(firstSwamp.getId(), secondSwamp.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstSwamp.getId(), secondSwamp.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returning no Swamps is legal and causes no discard")
    void returningNoSwampsCausesNoDiscard() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castAtPlayer2();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target yourself and discards for each Swamp returned")
    void canTargetYourself() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new SinkIntoTakenuma(), discardedCard)));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(swamp.getId()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(swamp.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    @DisplayName("Returns a controlled Swamp to its owner's hand")
    void returnsControlledSwampToItsOwnersHand() {
        Swamp swampCard = new Swamp();
        swampCard.setOwnerId(player2.getId());
        Permanent controlledSwamp = harness.addToBattlefieldAndReturn(player1, swampCard);
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setHand(player2, List.of(discardedCard));

        castAtPlayer2();
        harness.handleMultiplePermanentsChosen(player1, List.of(controlledSwamp.getId()));
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(controlledSwamp);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(swampCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
    }

    @Test
    @DisplayName("Can target only a player")
    void targetMustBePlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinkIntoTakenuma()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only chosen controlled Swamps contribute to the discard count")
    void returnsOnlyChosenControlledSwamps() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player2, List.of(new Swamp(), new Swamp()));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(chosen.getId(), unchosen.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposing);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen.getCard());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No controlled Swamps resolves without choices or discard")
    void noControlledSwampsCausesNoDiscard() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player2, List.of(new Swamp()));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposing);
        harness.assertInGraveyard(player1, "Sink into Takenuma");
    }

    @Test
    @DisplayName("Discards only the available cards when more Swamps are returned")
    void discardsOnlyAvailableCards() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Swamp discarded = new Swamp();
        harness.setHand(player2, List.of(discarded));

        castAtPlayer2();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first.getCard(), second.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Swamps still return when the target has an empty hand")
    void returnsSwampsWhenTargetHasNoCards() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player2, List.of());

        castAtPlayer2();
        harness.handleMultiplePermanentsChosen(player1, List.of(swamp.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(swamp.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Swamp exiled instead of returned does not cause a discard")
    void exiledSwampDoesNotContributeToDiscardCount() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.setExileIfLeavesBattlefield(true);
        Swamp cardInHand = new Swamp();
        harness.setHand(player2, List.of(cardInHand));

        castAtPlayer2();
        harness.handleMultiplePermanentsChosen(player1, List.of(swamp.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(swamp.getCard().getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(swamp.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cardInHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new SinkIntoTakenuma()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
