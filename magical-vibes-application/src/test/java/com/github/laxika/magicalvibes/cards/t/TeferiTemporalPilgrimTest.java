package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeferiTemporalPilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("Gains loyalty whenever its controller draws a card")
    void gainsLoyaltyOnControllerDraw() {
        Permanent teferi = addReadyTeferi(player1, 4);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("The -2 token gets a +1/+1 counter whenever its controller draws")
    void spiritGrowsOnControllerDraw() {
        Permanent teferi = addReadyTeferi(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ultimate lets the opponent choose one permanent, then shuffles their remaining nonlands")
    void ultimateReturnsChosenPermanentAndShufflesRemainingNonlands() {
        Permanent teferi = addReadyTeferi(player1, 12);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent shuffled = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(libraryCard));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(chosen.getId(), shuffled.getId(), land.getId());

        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).contains(chosen.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCard, shuffled.getCard());
    }

    @Test
    @DisplayName("The ultimate cannot target its controller")
    void ultimateRequiresOpponentTarget() {
        addReadyTeferi(player1, 12);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent perm = new Permanent(new TeferiTemporalPilgrim());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
