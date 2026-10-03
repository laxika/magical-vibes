package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaggersailAeronaut.class, ActOfTreason.class})
class DaggersailAeronautTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during its controller's turn only")
    void hasFlyingDuringControllerTurnOnly() {
        Permanent aeronaut = harness.addToBattlefieldAndReturn(player1, new DaggersailAeronaut());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying follows the controller, not the card owner")
    void flyingFollowsController() {
        Permanent ownAeronaut = harness.addToBattlefieldAndReturn(player1, new DaggersailAeronaut());
        Permanent enemyAeronaut = harness.addToBattlefieldAndReturn(player2, new DaggersailAeronaut());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, ownAeronaut, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, enemyAeronaut, Keyword.FLYING)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, ownAeronaut, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, enemyAeronaut, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gaining control immediately grants flying during the new controller's turn")
    void gainingControlUpdatesFlyingImmediately() {
        Permanent aeronaut = harness.addToBattlefieldAndReturn(player2, new DaggersailAeronaut());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, aeronaut.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aeronaut);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aeronaut);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING))
                .isEqualTo(gd.activePlayerId.equals(player2.getId()));
    }
}
