package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeferiMasterOfTimeTest extends BaseCardTest {

    @Test
    void plusOneDrawsThenDiscards() {
        Permanent teferi = addReadyTeferi(player1, 3);
        Shock keptInHand = new Shock();
        harness.setHand(player1, List.of(keptInHand));
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(keptInHand);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusThreePhasesOutCreatureAnOpponentControls() {
        Permanent teferi = addReadyTeferi(player1, 3);
        Permanent opposingCreature = addPermanent(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, opposingCreature.getId())).isNull();
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opposingCreature.getId()));
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void minusThreeCannotTargetYourCreature() {
        addReadyTeferi(player1, 3);
        Permanent ownCreature = addPermanent(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void loyaltyAbilitiesCanBeActivatedOnAnOpponentsTurnWithAStackPresent() {
        Permanent teferi = addReadyTeferi(player1, 3);
        Permanent opposingCreature = addPermanent(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, opposingCreature.getId())).isNull();
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void minusTenTakesTwoExtraTurns() {
        Permanent teferi = addReadyTeferi(player1, 10);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(teferi);
        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TeferiMasterOfTime());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
