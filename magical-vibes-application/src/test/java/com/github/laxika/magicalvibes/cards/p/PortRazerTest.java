package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({PortRazer.class, GrizzlyBears.class, ChandraNalaar.class})
class PortRazerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage untaps creatures and creates an additional combat")
    void combatDamageUntapsCreaturesAndCreatesAdditionalCombat() {
        Permanent portRazer = addCreatureReady(player1, new PortRazer());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(bear.isTapped()).isFalse();
        assertThat(portRazer.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    @Test
    @DisplayName("Cannot attack the same player again, but another creature can")
    void cannotAttackPreviouslyAttackedPlayer() {
        addCreatureReady(player1, new PortRazer());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        declareAttackers(List.of(1));
    }

    @Test
    @DisplayName("Combat damage creates one ability containing both instructions")
    void combatDamageCreatesOneTriggeredAbility() {
        addCreatureReady(player1, new PortRazer());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Only the controller's creatures untap")
    void doesNotUntapOpposingCreaturesOrNoncreatures() {
        Permanent attacker = addCreatureReady(player1, new PortRazer());
        Permanent friendly = addCreatureReady(player1, new PortRazer());
        Permanent opposing = addCreatureReady(player2, new PortRazer());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        noncreature.setCounterCount(CounterType.LOYALTY, 6);
        friendly.tap();
        opposing.tap();
        noncreature.tap();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(friendly.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(noncreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blocked combat does not trigger an untap or additional combat")
    void blockedCombatDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new PortRazer());
        Permanent friendly = addCreatureReady(player1, new PortRazer());
        addCreatureReady(player2, new GrizzlyBears());
        friendly.tap();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(attacker.isTapped()).isTrue();
        assertThat(friendly.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("additional combat phase")).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a planeswalker does not trigger")
    void planeswalkerDamageDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new PortRazer());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        resolveCombat();
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("additional combat phase")).isFalse();
    }

    @Test
    @DisplayName("Can attack a planeswalker controlled by a player already attacked")
    void canAttackPlaneswalkerControlledByPreviouslyAttackedPlayer() {
        Permanent portRazer = addCreatureReady(player1, new PortRazer());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(portRazer)),
                Map.of(gd.playerBattlefields.get(player1.getId()).indexOf(portRazer), planeswalker.getId())));

        assertThat(portRazer.isAttacking()).isTrue();
        assertThat(portRazer.getAttackTarget()).isEqualTo(planeswalker.getId());
    }
}
