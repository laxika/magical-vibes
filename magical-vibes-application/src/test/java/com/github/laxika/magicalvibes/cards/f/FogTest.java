package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fog.class, FeralShadow.class, BayFalcon.class, GiantMantis.class, Incinerate.class})
class FogTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fog puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage to players and creatures")
    void preventsCombatDamageToPlayersAndCreatures() {
        Permanent blockedAttacker = addCreatureReady(player1, new FeralShadow());
        Permanent unblockedAttacker = addCreatureReady(player1, new BayFalcon());
        Permanent blocker = addCreatureReady(player2, new GiantMantis());
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0, 1));
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(blockedAttacker.getMarkedDamage()).isZero();
        assertThat(unblockedAttacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(blockedAttacker, unblockedAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blocker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setHand(player1, List.of(new Fog(), new Incinerate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Combat damage prevention ends at end of turn")
    void combatDamagePreventionEndsAtEndOfTurn() {
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    @Test
    @DisplayName("Fog goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fog");
    }
}
