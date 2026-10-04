package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Feint.class, BarbaryApes.class})
class FeintTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all blockers and prevents combat damage from the target and those blockers")
    void tapsBlockersAndPreventsTheirCombatDamage() {
        Permanent targetAttacker = addCreatureReady(player1, new BarbaryApes());
        addCreatureReady(player1, new BarbaryApes());
        Permanent firstBlocker = addCreatureReady(player2, new BarbaryApes());
        Permanent secondBlocker = addCreatureReady(player2, new BarbaryApes());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        harness.setHand(player1, List.of(new Feint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetAttacker.getId());
        harness.passBothPriorities();

        assertThat(firstBlocker.isTapped()).isTrue();
        assertThat(secondBlocker.isTapped()).isTrue();
        resolveCombat();

        assertThat(targetAttacker.getMarkedDamage()).isZero();
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents combat damage from an unblocked target attacker")
    void preventsUnblockedTargetAttackerCombatDamage() {
        Permanent targetAttacker = addCreatureReady(player1, new BarbaryApes());
        addCreatureReady(player1, new BarbaryApes());
        Permanent otherBlocker = addCreatureReady(player2, new BarbaryApes());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.setHand(player1, List.of(new Feint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherBlocker.isTapped()).isFalse();
        resolveCombat();

        assertThat(targetAttacker.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blockers declared after Feint resolves still deal combat damage")
    void laterBlockersStillDealCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new BarbaryApes());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new Feint()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player1, 0, attacker.getId());
            harness.passBothPriorities();
        });

        assertThat(blocker.isTapped()).isFalse();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can target only an attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new Feint()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }
}
