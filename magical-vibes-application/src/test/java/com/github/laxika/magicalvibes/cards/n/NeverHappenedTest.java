package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeverHappenedTest extends BaseCardTest {

    @Test
    void choosesANonlandFromHandOrGraveyardAndExilesIt() {
        Card land = new Forest();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(land, handCard)));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castNeverHappened();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, handCard);
    }

    @Test
    void doesNotGrantPermissionToCastTheExiledCard() {
        Card exiledCard = new GrizzlyBears();
        harness.setHand(player2, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(exiledCard));

        castNeverHappened();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, exiledCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new NeverHappened()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    private void castNeverHappened() {
        harness.setHand(player1, List.of(new NeverHappened()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
