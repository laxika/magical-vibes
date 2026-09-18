package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TheGrandTour.class, GrizzlyBears.class, Shock.class})
class TheGrandTourTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a permanent, makes its owner discard it, and returns it to the battlefield")
    void exilesDiscardsAndReturnsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card unrelated = new Shock();
        harness.setHand(player2, new ArrayList<>(List.of(unrelated)));
        harness.setHand(player1, List.of(new TheGrandTour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(unrelated.getId(), target.getCard().getId());

        harness.handleCardChosen(player2, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactly(target.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .containsExactly(unrelated.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
