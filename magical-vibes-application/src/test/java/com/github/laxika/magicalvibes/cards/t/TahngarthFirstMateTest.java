package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TahngarthFirstMate.class, SakuraTribeElder.class, GarrukPrimalHunter.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class TahngarthFirstMateTest extends BaseCardTest {

    @Test
    @DisplayName("It cannot be blocked by more than one creature")
    void cannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new TahngarthFirstMate());
        addCreatureReady(player2, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("When tapped, it may join an opponent's attack under that opponent's control")
    void tappedTahngarthJoinsOpponentsAttack() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        addCreatureReady(player2, new SakuraTribeElder());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tahngarth);
        assertThat(tahngarth.isAttacking()).isTrue();
        assertThat(tahngarth.getAttackTarget()).isEqualTo(player1.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tahngarth);
    }

    @Test
    @DisplayName("The ability does not trigger while Tahngarth is untapped")
    void untappedTahngarthDoesNotTrigger() {
        addCreatureReady(player1, new TahngarthFirstMate());
        addCreatureReady(player2, new SakuraTribeElder());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining leaves Tahngarth under its controller's control")
    void decliningLeavesControlUnchanged() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        addCreatureReady(player2, new SakuraTribeElder());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tahngarth);
        assertThat(tahngarth.isAttacking()).isFalse();
    }

    @Test
    void canBeBlockedByOneCreature() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        Permanent blocker = addCreatureReady(player2, new SakuraTribeElder());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(tahngarth.isAttacking()).isTrue();
    }

    @Test
    void ownAttackDoesNotTriggerControlTransfer() {
        addCreatureReady(player1, new TahngarthFirstMate());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void becomingUntappedBeforeResolutionPreventsControlTransfer() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        addCreatureReady(player2, new SakuraTribeElder());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            assertThat(gd.stack).hasSize(1);
            tahngarth.untap();
            resolveAllTriggers();
        });

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tahngarth);
        assertThat(tahngarth.isAttacking()).isFalse();
    }

    @Test
    void multipleOpponentAttackersProduceOnlyOneControlChoice() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        addCreatureReady(player2, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0, 1));
            assertThat(gd.stack).hasSize(1);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(tahngarth.isAttacking()).isTrue();
        assertThat(tahngarth.isTapped()).isTrue();
        assertThat(tahngarth.getAttackTarget()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tahngarth);
    }

    @Test
    void noRemainingAttackersStillAllowsControlTransferWithoutAttacking() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        Permanent attacker = addCreatureReady(player2, new SakuraTribeElder());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            attacker.setAttacking(false);
            attacker.setAttackTarget(null);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tahngarth);
        assertThat(tahngarth.isAttacking()).isFalse();
        assertThat(tahngarth.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void joinsAttackOnPlaneswalkerRatherThanItsController() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new GarrukPrimalHunter());
        garruk.setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player2, new SakuraTribeElder());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player2, List.of(0), Map.of(0, garruk.getId()));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(tahngarth.isAttacking()).isTrue();
        assertThat(tahngarth.getAttackTarget()).isEqualTo(garruk.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tahngarth);
    }

    @Test
    void battleOnlyAttackTransfersControlWithoutMakingTahngarthAttackOrAskingForAChoice() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        addCreatureReady(player2, new SakuraTribeElder());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player2, List.of(1), Map.of(1, battle.getId()));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tahngarth);
        assertThat(tahngarth.isAttacking()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
