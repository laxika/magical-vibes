package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.t.TeferiTimeRaveler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuhanOfTheFomori.class, GhostlyPrison.class, TeferiTimeRaveler.class})
class RuhanOfTheFomoriTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's combat, Ruhan chooses the opponent and must attack this combat")
    void choosesOpponentAndMustAttackThisCombat() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(ruhan.isMustAttackThisCombat()).isTrue();
        assertThat(ruhan.getMustAttackTargetId()).isEqualTo(player2.getId());

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Ruhan's requirement is combat-scoped")
    void requirementIsCombatScoped() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(ruhan.isMustAttackThisCombat()).isTrue();

        ruhan.clearCombatState();

        assertThat(ruhan.isMustAttackThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Ruhan does not trigger on an opponent's combat")
    void doesNotTriggerOnOpponentsCombat() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(ruhan.isMustAttackThisCombat()).isFalse();
    }

    @Test
    void requirementExpiresWhenCombatEnds() {
        Permanent ruhan = addReadyRuhan(player1);
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(ruhan.isMustAttackThisCombat()).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(ruhan.isMustAttackThisCombat()).isFalse();
    }

    @Test
    void attacksChosenPlayerWhenAble() {
        Permanent ruhan = addReadyRuhan(player1);
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(ruhan.isAttacking()).isTrue();
        assertThat(ruhan.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    void summoningSickRuhanIsNotRequiredToAttack() {
        Permanent ruhan = harness.addToBattlefieldAndReturn(player1, new RuhanOfTheFomori());
        ruhan.setSummoningSick(true);
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(ruhan.isAttacking()).isFalse();
    }

    @Test
    void tappedRuhanIsNotRequiredToAttack() {
        Permanent ruhan = addReadyRuhan(player1);
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        ruhan.setTapped(true);
        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(ruhan.isAttacking()).isFalse();
    }

    @Test
    void cannotAttackPlaneswalkerInsteadOfChosenPlayer() {
        addReadyRuhan(player1);
        Permanent teferi = harness.addToBattlefieldAndReturn(player2, new TeferiTimeRaveler());
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0),
                Map.of(0, teferi.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayDeclineTaxedAttackEvenWhenPlaneswalkerCanBeAttackedForFree() {
        Permanent ruhan = addReadyRuhan(player1);
        harness.addToBattlefield(player2, new GhostlyPrison());
        harness.addToBattlefield(player2, new TeferiTimeRaveler());
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(ruhan.isAttacking()).isFalse();
    }

    @Test
    void mayAttackPlaneswalkerInsteadOfPayingChosenPlayersAttackTax() {
        Permanent ruhan = addReadyRuhan(player1);
        harness.addToBattlefield(player2, new GhostlyPrison());
        Permanent teferi = harness.addToBattlefieldAndReturn(player2, new TeferiTimeRaveler());
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, teferi.getId()));

        assertThat(ruhan.isAttacking()).isTrue();
        assertThat(ruhan.getAttackTarget()).isEqualTo(teferi.getId());
    }

    private Permanent addReadyRuhan(Player player) {
        Permanent ruhan = harness.addToBattlefieldAndReturn(player, new RuhanOfTheFomori());
        ruhan.setSummoningSick(false);
        return ruhan;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
