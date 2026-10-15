package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlyPrison.class, HumbleBudoka.class, JaceBeleren.class, InvasionOfZendikar.class})
class GhostlyPrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent pays {2} for one creature attacking the controller")
    void opponentPaysTwoPerAttacker() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        addCreatureReady(player2, new HumbleBudoka());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent pays {2} for each creature attacking the controller")
    void opponentPaysTwoForEachAttacker() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        addCreatureReady(player2, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Opponent cannot attack the controller without paying the tax")
    void opponentCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        addCreatureReady(player2, new HumbleBudoka());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Attacking the controller's planeswalker does not require the player-only tax")
    void planeswalkerIsNotTaxed() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player2, new HumbleBudoka());

        declareAttackersAtTarget(player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Prisons add their attack costs")
    void multiplePrisonsStack() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        harness.addToBattlefield(player1, new GhostlyPrison());
        addCreatureReady(player2, new HumbleBudoka());
        harness.addMana(player2, ManaColor.GREEN, 4);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An unaffordable declaration spends no mana and taps no attackers")
    void insufficientManaDoesNotPartiallyPay() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        Permanent first = addCreatureReady(player2, new HumbleBudoka());
        Permanent second = addCreatureReady(player2, new HumbleBudoka());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.isAttacking()).isFalse();
        assertThat(second.isAttacking()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the creature attacking the player pays in a mixed declaration")
    void mixedAttackTargetsTaxOnlyPlayerAttack() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player2, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackersAtTarget(player2, List.of(0, 1),
                Map.of(0, player1.getId(), 1, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Prison does not tax its controller's attacks")
    void controllerCanAttackWithoutPayment() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        addCreatureReady(player1, new HumbleBudoka());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void declareAttackersAtTarget(Player player, List<Integer> attackerIndices,
                                          Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    @Test
    @DisplayName("Attacking a battle protected by the controller does not require payment")
    void battleIsNotTaxed() {
        harness.addToBattlefield(player1, new GhostlyPrison());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        addCreatureReady(player2, new HumbleBudoka());

        declareAttackersAtTarget(player2, List.of(1), Map.of(1, battle.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

}
