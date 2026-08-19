package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

class AngrathTheFlameChainedTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes each opponent discard a card and lose 2 life")
    void plusOneDiscardsAndLosesLife() {
        Permanent angrath = addReadyAngrath(player1, 4);
        harness.setHand(player2, List.of(new Forest()));
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("-3 steals, untaps, hastes, and sacrifices a low-mana-value creature at the next end step")
    void minusThreeStealsAndSacrificesLowManaValueCreature() {
        Permanent angrath = addReadyAngrath(player1, 4);
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-3 does not sacrifice a creature whose mana value is greater than 3")
    void minusThreeSkipsHighManaValueCreature() {
        addReadyAngrath(player1, 4);
        Permanent target = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("-3 can target only a creature")
    void minusThreeRejectsNoncreatureTarget() {
        addReadyAngrath(player1, 4);
        Permanent target = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 makes each opponent lose life equal to cards in their graveyard")
    void minusEightCountsOpponentsGraveyard() {
        Permanent angrath = addReadyAngrath(player1, 8);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new Forest(), new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private Permanent addReadyAngrath(Player player, int loyalty) {
        Permanent permanent = new Permanent(new AngrathTheFlameChained());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
