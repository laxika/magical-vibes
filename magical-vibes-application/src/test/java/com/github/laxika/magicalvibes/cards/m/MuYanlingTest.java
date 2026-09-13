package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuYanling.class, GrizzlyBears.class})
class MuYanlingTest extends BaseCardTest {

    @Test
    @DisplayName("+2 makes a target creature unable to be blocked this turn")
    void plusTwoMakesTargetUnblockable() {
        Permanent muYanling = addReadyMuYanling(player1, 5);
        Permanent target = addCreature(player2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(muYanling.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("+2 cannot target a noncreature permanent")
    void plusTwoRejectsNoncreatureTarget() {
        Permanent muYanling = addReadyMuYanling(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, muYanling.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−3 draws two cards")
    void minusThreeDrawsTwoCards() {
        Permanent muYanling = addReadyMuYanling(player1, 5);
        List<com.github.laxika.magicalvibes.model.Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(muYanling.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("−10 taps opposing creatures and grants an extra turn")
    void minusTenTapsOpposingCreaturesAndGrantsExtraTurn() {
        Permanent muYanling = addReadyMuYanling(player1, 10);
        Permanent ownCreature = addCreature(player1);
        Permanent opposingCreature = addCreature(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(muYanling.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyMuYanling(Player player, int loyalty) {
        Permanent permanent = new Permanent(new MuYanling());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
