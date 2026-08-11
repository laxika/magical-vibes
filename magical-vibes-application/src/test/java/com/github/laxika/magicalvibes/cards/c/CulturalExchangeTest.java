package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CulturalExchangeTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new CulturalExchange()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Exchanges control of equal numbers of creatures chosen from both players")
    void exchangesChosenCreatures() {
        prepare();
        Permanent ownFirst = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownSecond = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentFirst = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentSecond = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownFirst.getId(), ownSecond.getId()));
        harness.handleMultiplePermanentsChosen(player1,
                List.of(opponentFirst.getId(), opponentSecond.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(opponentFirst, opponentSecond);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(ownFirst, ownSecond);
    }

    @Test
    @DisplayName("Choosing no creatures makes the exchange do nothing")
    void choosingNoCreaturesDoesNothing() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponent);
    }

    @Test
    @DisplayName("Having no creatures to exchange resolves without a choice")
    void noCreaturesToExchangeResolvesImmediately() {
        prepare();

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The two target players must be different")
    void cannotTargetTheSamePlayerTwice() {
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
