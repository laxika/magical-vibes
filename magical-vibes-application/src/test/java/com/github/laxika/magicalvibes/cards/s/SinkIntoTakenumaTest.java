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

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
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

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new SinkIntoTakenuma()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
