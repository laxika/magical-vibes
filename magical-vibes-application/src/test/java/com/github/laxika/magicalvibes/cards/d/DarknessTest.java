package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({Darkness.class, BarbaryApes.class, Pyrotechnics.class})
class DarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Darkness prevents all combat damage this turn")
    void preventsAllCombatDamageThisTurn() {
        harness.setHand(player1, List.of(new Darkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Darkness combat damage prevention ends at end of turn")
    void combatDamagePreventionEndsAtEndOfTurn() {
        harness.setHand(player1, List.of(new Darkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    @Test
    @DisplayName("Darkness goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Darkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Darkness");
    }

    @Test
    @DisplayName("Prevents combat damage to players and creatures")
    void preventsCombatDamageToPlayersAndCreatures() {
        Permanent blockedAttacker = addCreatureReady(player1, new BarbaryApes());
        Permanent unblockedAttacker = addCreatureReady(player1, new BarbaryApes());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Darkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(blockedAttacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(blockedAttacker, unblockedAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blocker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Darkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, Map.of(player2.getId(), 4));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
