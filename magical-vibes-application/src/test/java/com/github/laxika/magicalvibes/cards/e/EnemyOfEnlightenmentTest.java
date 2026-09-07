package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnemyOfEnlightenment.class, GrizzlyBears.class})
class EnemyOfEnlightenmentTest extends BaseCardTest {

    @Test
    @DisplayName("Its power and toughness change with the opponent's hand size")
    void scalesWithOpponentHandSize() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.addToBattlefield(player1, new EnemyOfEnlightenment());
        Permanent enemy = findPermanent(player1, "Enemy of Enlightenment");

        int powerBeforeHandChange = gqs.getEffectivePower(gd, enemy);
        int toughnessBeforeHandChange = gqs.getEffectiveToughness(gd, enemy);

        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(powerBeforeHandChange - 1);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(toughnessBeforeHandChange - 1);
    }

    @Test
    @DisplayName("At the controller's upkeep, each player discards a card")
    void eachPlayerDiscardsOnControllerUpkeep() {
        harness.addToBattlefield(player1, new EnemyOfEnlightenment());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
