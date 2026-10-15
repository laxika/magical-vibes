package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NooseConstrictor.class, GrizzlyBears.class, Forest.class, NebelgastHerald.class})
class NooseConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Discard a card gives this creature +1/+1 until end of turn")
    void discardBoostsPlusOneOne() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent constrictor = harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        int basePower = gqs.getEffectivePower(gd, constrictor);
        int baseToughness = gqs.getEffectiveToughness(gd, constrictor);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0); // pay the discard cost
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent constrictor = harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        int basePower = gqs.getEffectivePower(gd, constrictor);
        int baseToughness = gqs.getEffectiveToughness(gd, constrictor);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Repeated activations pay separate discard costs and their boosts stack")
    void repeatedActivationsStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent constrictor = harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        int basePower = gqs.getEffectivePower(gd, constrictor);
        int baseToughness = gqs.getEffectiveToughness(gd, constrictor);
        harness.setHand(player1, List.of(new NooseConstrictor(), new NooseConstrictor()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("A tapped summoning-sick Constrictor can activate during an opponent's turn")
    void canActivateWhileTappedOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent constrictor = harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        constrictor.tap();
        constrictor.setSummoningSick(true);
        int basePower = gqs.getEffectivePower(gd, constrictor);
        int baseToughness = gqs.getEffectiveToughness(gd, constrictor);
        harness.setHand(player1, List.of(new Forest()));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(constrictor.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, constrictor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, constrictor)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Reach allows Noose Constrictor to block a flying creature")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new NebelgastHerald());
        attacker.setAttacking(true);
        Permanent constrictor = addCreatureReady(player2, new NooseConstrictor());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(constrictor.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new NooseConstrictor());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
