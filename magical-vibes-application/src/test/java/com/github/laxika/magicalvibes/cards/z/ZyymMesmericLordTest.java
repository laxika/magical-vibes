package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZyymMesmericLord.class, Forest.class, DarkRitual.class, GrizzlyBears.class})
class ZyymMesmericLordTest extends BaseCardTest {

    @Test
    @DisplayName("the controller reveals in the opponent's chosen order and discards the last reveal")
    void revealsUntilControllerStops() {
        Card forest = new Forest();
        Card ritual = new DarkRitual();
        Card bears = new GrizzlyBears();
        resolveZyym(List.of(forest, ritual, bears));

        PendingInteraction.TargetPlayerHandOrderChoice order =
                gd.interaction.activeInteraction(PendingInteraction.TargetPlayerHandOrderChoice.class);
        assertThat(order).isNotNull();
        assertThat(order.playerId()).isEqualTo(player2.getId());
        assertThat(order.cards()).containsExactly(forest, ritual, bears);

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, ritual);
    }

    @Test
    @DisplayName("stopping before the first reveal does not discard a card")
    void canStopBeforeRevealing() {
        Card forest = new Forest();
        Card ritual = new DarkRitual();
        resolveZyym(List.of(forest, ritual));

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.CardOrder(List.of(1, 0)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, ritual);
    }

    private void resolveZyym(List<Card> opponentHand) {
        harness.setHand(player2, new ArrayList<>(opponentHand));
        harness.setHand(player1, List.of(new ZyymMesmericLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
