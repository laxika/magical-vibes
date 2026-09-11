package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacklingFiend.class, CoralMerfolk.class})
class CacklingFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Cackling Fiend puts its ETB trigger on the stack")
    void resolvingPutsEtbOnStack() {
        castCacklingFiend();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cackling Fiend");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB trigger makes each opponent discard a card")
    void etbMakesOpponentDiscard() {
        harness.setHand(player2, List.of(new CoralMerfolk(), new CoralMerfolk()));
        castCacklingFiend();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("ETB trigger does nothing when the opponent has no cards in hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        castCacklingFiend();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCacklingFiend() {
        harness.castFromHand(player1, new CacklingFiend(), "{2}{B}{B}");
    }
}
