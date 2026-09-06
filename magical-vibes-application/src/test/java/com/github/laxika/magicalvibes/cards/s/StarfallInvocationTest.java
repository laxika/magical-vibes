package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfallInvocation.class, GrizzlyBears.class})
class StarfallInvocationTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesWithoutGift() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player2, opposingCreature);

        cast(false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(ownCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(opposingCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void giftReturnsOneCreatureDestroyedIntoYourGraveyard() {
        Card oldGraveyardCreature = new GrizzlyBears();
        Card ownCreature = new GrizzlyBears();
        Card secondOwnCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldGraveyardCreature));
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player1, secondOwnCreature);
        harness.addToBattlefield(player2, opposingCreature);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(true);

        PendingInteraction.GraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1, 2);
        assertThat(choice.mandatory()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);

        harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(choice.validIndices().getFirst().equals(1)
                        ? ownCreature.getId() : secondOwnCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(oldGraveyardCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(opposingCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(boolean giftPromised) {
        harness.setHand(player1, List.of(new StarfallInvocation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorceryWithGift(player1, 0, List.of(), giftPromised);
        harness.passBothPriorities();
    }
}
