package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgonizingRemorse.class, Forest.class, GrizzlyBears.class, Peek.class})
class AgonizingRemorseTest extends BaseCardTest {

    @Test
    void choosesANonlandFromHandAndExilesItThenYouLoseLife() {
        Card land = new Forest();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Peek();
        harness.setHand(player2, List.of(land, handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        int lifeBefore = gd.getLife(player1.getId());

        castAgonizingRemorse();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void canChooseANonlandFromTheTargetGraveyard() {
        Card land = new Forest();
        Card graveyardCard = new Peek();
        harness.setHand(player2, List.of(land));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castAgonizingRemorse();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotGrantPermissionToCastTheExiledCard() {
        Card exiledCard = new GrizzlyBears();
        harness.setHand(player2, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(exiledCard));

        castAgonizingRemorse();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, exiledCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new AgonizingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    private void castAgonizingRemorse() {
        harness.setHand(player1, List.of(new AgonizingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
