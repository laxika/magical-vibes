package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ComingInHot;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarTrainedSlasher.class, InvasionOfZendikar.class, ComingInHot.class})
class WarTrainedSlasherTest extends BaseCardTest {

    @Test
    void doublesPowerWhenAttackingBattle() {
        Permanent battle = addBattle(player1);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());

        declareAttackersAt(battle);
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isEqualTo(4);
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void doesNotTriggerWhenAttackingPlayer() {
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isZero();
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void powerBoostEndsAtCleanup() {
        Permanent battle = addBattle(player1);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());

        declareAttackersAt(battle);
        resolveAllTriggers();
        assertThat(slasher.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(slasher.getPowerModifier()).isZero();
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void doublesPowerMeasuredWhenTriggerResolves() {
        Permanent battle = addBattle(player1);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ComingInHot()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackersAt(battle);
        harness.castInstant(player1, 0, slasher.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isEqualTo(6);
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void doublesNegativePower() {
        Permanent battle = addBattle(player1);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());
        slasher.setPowerModifier(-5);

        declareAttackersAt(battle);
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isEqualTo(-6);
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void zeroPowerRemainsZero() {
        Permanent battle = addBattle(player1);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());
        slasher.setPowerModifier(-4);

        declareAttackersAt(battle);
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isEqualTo(-4);
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void stillDoublesPowerAfterAttackedBattleLeaves() {
        Permanent battle = addBattle(player1);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());

        declareAttackersAt(battle);
        gd.playerBattlefields.get(player1.getId()).remove(battle);
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isEqualTo(4);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new WarTrainedSlasher());
        addCreatureReady(player2, new WarTrainedSlasher());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new WarTrainedSlasher());
        Permanent firstBlocker = addCreatureReady(player2, new WarTrainedSlasher());
        Permanent secondBlocker = addCreatureReady(player2, new WarTrainedSlasher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    private Permanent addBattle(Player controller) {
        Permanent battle = harness.addToBattlefieldAndReturn(controller, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());
        return battle;
    }

    private void declareAttackersAt(Permanent battle) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1), Map.of(1, battle.getId()));
    }
}
