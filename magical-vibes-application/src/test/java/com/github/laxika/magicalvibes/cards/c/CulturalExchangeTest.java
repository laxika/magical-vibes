package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.a.AvenSmokeweaver;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CulturalExchange.class, AvenFisher.class, AvenSmokeweaver.class, Island.class})
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
        Permanent ownFirst = harness.addToBattlefieldAndReturn(player1, new AvenFisher());
        Permanent ownSecond = harness.addToBattlefieldAndReturn(player1, new AvenSmokeweaver());
        Permanent opponentFirst = harness.addToBattlefieldAndReturn(player2, new AvenFisher());
        Permanent opponentSecond = harness.addToBattlefieldAndReturn(player2, new AvenSmokeweaver());

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
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
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AvenFisher());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AvenSmokeweaver());

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponent);
    }

    @Test
    @DisplayName("Having no creatures to exchange resolves without a choice")
    void noCreaturesToExchangeResolvesImmediately() {
        prepare();

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only creatures are eligible for the exchange")
    void onlyCreaturesCanBeChosen() {
        prepare();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AvenFisher());
        Permanent ownUnchosenCreature = harness.addToBattlefieldAndReturn(player1, new AvenSmokeweaver());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AvenFisher());

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(ownLand.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(ownCreature.getId(), ownUnchosenCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(ownUnchosenCreature, ownLand, opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(ownCreature);
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
