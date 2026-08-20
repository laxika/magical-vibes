package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeralDeathgorgerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to two cards from a single graveyard")
    void etbExilesUpToTwoCardsFromSingleGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new Plains();
        harness.setGraveyard(player2, List.of(first, second, third));
        FeralDeathgorger card = new FeralDeathgorger();
        harness.setHand(player1, List.of(card));
        addManaForCreature();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Omen puts a counter on a creature, draws, and shuffles the card into its owner's library")
    void omenPutsCounterDrawsAndShuffles() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new Plains();
        FeralDeathgorger card = new FeralDeathgorger();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, target.getId(), null, List.of());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    private void addManaForCreature() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
