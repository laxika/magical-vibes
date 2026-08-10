package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntropicSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the opponent's hand size")
    void powerAndToughnessEqualOpponentHandSize() {
        harness.setHand(player1, handOf(5));
        harness.setHand(player2, handOf(3));
        Permanent specter = addSpecter(player1);

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update as the opponent's hand changes")
    void powerAndToughnessUpdateWithOpponentHand() {
        harness.setHand(player2, handOf(2));
        Permanent specter = addSpecter(player1);

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(2);

        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage to a player makes that player discard a card")
    void damageToPlayerTriggersDiscard() {
        harness.setHand(player2, handOf(2));
        Permanent specter = addSpecter(player1);
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private Permanent addSpecter(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new EntropicSpecter());
    }

    private List<Card> handOf(int count) {
        return new ArrayList<>(java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> (Card) new GrizzlyBears())
                .toList());
    }
}
