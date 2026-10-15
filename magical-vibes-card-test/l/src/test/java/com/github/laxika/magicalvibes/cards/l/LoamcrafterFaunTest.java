package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoamcrafterFaun.class, Forest.class, GrizzlyBears.class, Shock.class})
class LoamcrafterFaunTest extends BaseCardTest {

    @Test
    void returnsUpToTheNumberOfDiscardedLandsAndOnlyNonlandPermanents() {
        Card firstReturned = new GrizzlyBears();
        Card secondReturned = new GrizzlyBears();
        Card instant = new Shock();
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setGraveyard(player1, List.of(firstReturned, instant, secondReturned));
        harness.setHand(player1, List.of(new LoamcrafterFaun(), firstLand, secondLand));
        addFaunMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(firstReturned.getId(), secondReturned.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstReturned.getId(), secondReturned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstReturned, secondReturned);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstLand, secondLand, instant);
    }

    @Test
    void decliningTheAbilityDoesNotDiscardOrReturnCards() {
        Card graveyardPermanent = new GrizzlyBears();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new LoamcrafterFaun(), land));
        addFaunMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardPermanent);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addFaunMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
