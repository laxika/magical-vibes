package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PauseForReflection.class, VernadiShieldmate.class, DirectCurrent.class})
class PauseForReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new PauseForReflection()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Convoke taps a creature and still prevents all combat damage")
    void convokeTapsCreature() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new PauseForReflection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreature.getId()));
        harness.passBothPriorities();

        assertThat(convokeCreature.isTapped()).isTrue();
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage while the effect is active")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PauseForReflection()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player2, new VernadiShieldmate());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void preventsDamageFromBothAttackersAndBlockers() {
        Permanent attacker = addCreatureReady(player1, new VernadiShieldmate());
        Permanent blocker = addCreatureReady(player2, new VernadiShieldmate());
        harness.setHand(player2, List.of(new PauseForReflection()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotPreventNoncombatDamage() {
        harness.setHand(player1, List.of(new PauseForReflection(), new DirectCurrent()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void convokeCanPayEntireCostWithSummoningSickCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new PauseForReflection()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.preventAllCombatDamage).isTrue();
        harness.assertInGraveyard(player1, "Pause for Reflection");
    }

    @Test
    void combatDamageResumesNextTurn() {
        harness.setHand(player1, List.of(new PauseForReflection()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new VernadiShieldmate());
        attacker.setAttacking(true);
        harness.setLife(player1, 20);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }
}
