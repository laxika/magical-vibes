package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.IlysianCaryatid;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnemyOfEnlightenment.class, IlysianCaryatid.class})
class EnemyOfEnlightenmentTest extends BaseCardTest {

    @Test
    @DisplayName("Its power and toughness change with the opponent's hand size")
    void scalesWithOpponentHandSize() {
        harness.setHand(player2, List.of(new IlysianCaryatid(), new IlysianCaryatid()));
        Permanent enemy = harness.addToBattlefieldAndReturn(player1, new EnemyOfEnlightenment());

        int powerBeforeHandChange = gqs.getEffectivePower(gd, enemy);
        int toughnessBeforeHandChange = gqs.getEffectiveToughness(gd, enemy);

        gd.playerHands.get(player2.getId()).add(new IlysianCaryatid());

        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(powerBeforeHandChange - 1);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(toughnessBeforeHandChange - 1);
    }

    @Test
    @DisplayName("At the controller's upkeep, each player discards a card")
    void eachPlayerDiscardsOnControllerUpkeep() {
        harness.addToBattlefield(player1, new EnemyOfEnlightenment());
        harness.setHand(player1, List.of(new IlysianCaryatid()));
        harness.setHand(player2, List.of(new IlysianCaryatid()));

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

    @Test
    @DisplayName("The controller's hand does not contribute to the penalty")
    void ignoresControllerHand() {
        harness.setHand(player1, List.of(new IlysianCaryatid(), new IlysianCaryatid()));
        harness.setHand(player2, List.of());
        Permanent enemy = harness.addToBattlefieldAndReturn(player1, new EnemyOfEnlightenment());
        int power = gqs.getEffectivePower(gd, enemy);
        int toughness = gqs.getEffectiveToughness(gd, enemy);

        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("Discarding from the opponent's hand immediately reduces the penalty")
    void discardReducesPenalty() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new IlysianCaryatid(), new IlysianCaryatid()));
        Permanent enemy = harness.addToBattlefieldAndReturn(player1, new EnemyOfEnlightenment());
        int power = gqs.getEffectivePower(gd, enemy);
        int toughness = gqs.getEffectiveToughness(gd, enemy);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Ilysian Caryatid");
        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(toughness + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty opponent hand does not stop the controller from discarding")
    void emptyOpponentHandStillDiscardsControllerCard() {
        harness.setHand(player1, List.of(new IlysianCaryatid()));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new EnemyOfEnlightenment());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ilysian Caryatid");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The upkeep ability resolves without a choice when both hands are empty")
    void bothHandsEmpty() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new EnemyOfEnlightenment());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Enemy of Enlightenment");
    }

    @Test
    @DisplayName("The opponent's upkeep does not trigger discarding")
    void noTriggerOnOpponentUpkeep() {
        harness.setHand(player1, List.of(new IlysianCaryatid()));
        harness.setHand(player2, List.of(new IlysianCaryatid()));
        harness.addToBattlefield(player1, new EnemyOfEnlightenment());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
