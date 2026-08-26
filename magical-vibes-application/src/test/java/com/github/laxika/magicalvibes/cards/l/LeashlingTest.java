package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Leashling.class, GrizzlyBears.class})
class LeashlingTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a card from hand on top of the library returns Leashling to its owner's hand")
    void putsCardOnTopAndReturnsToHand() {
        Permanent leashling = addCreatureReady(player1, new Leashling());
        Card chosenCard = new GrizzlyBears();
        Card existingTopCard = new GrizzlyBears();
        harness.setHand(player1, List.of(chosenCard));
        harness.setLibrary(player1, List.of(existingTopCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(leashling);
        assertThat(gd.playerHands.get(player1.getId())).contains(leashling.getOriginalCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosenCard, existingTopCard);
    }

    @Test
    @DisplayName("The ability cannot be activated without a card in hand")
    void cannotActivateWithoutCardInHand() {
        addCreatureReady(player1, new Leashling());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Leashling");
    }
}
