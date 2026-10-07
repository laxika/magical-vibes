package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FootNinjas;
import com.github.laxika.magicalvibes.cards.m.MouserMarkIII;
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

@CardUsed({SplinterHamatoYoshi.class, FootNinjas.class, MouserMarkIII.class})
class SplinterHamatoYoshiTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other Ninjas you control +1/+1")
    void boostsOtherNinjasYouControl() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterHamatoYoshi());
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new FootNinjas());
        Permanent nonNinja = harness.addToBattlefieldAndReturn(player1, new MouserMarkIII());
        Permanent opposingNinja = harness.addToBattlefieldAndReturn(player2, new FootNinjas());

        assertThat(gqs.getEffectivePower(gd, splinter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, splinter)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonNinja)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonNinja)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingNinja)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingNinja)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and puts Splinter in tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new FootNinjas());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new SplinterHamatoYoshi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
            harness.assertInHand(player1, "Foot Ninjas");
            harness.assertNotOnBattlefield(player1, "Foot Ninjas");
            harness.assertNotOnBattlefield(player1, "Splinter, Hamato Yoshi");
            harness.passBothPriorities();
        });

        harness.assertInHand(player1, "Foot Ninjas");
        Permanent splinter = findPermanent(player1, "Splinter, Hamato Yoshi");
        assertThat(splinter.isTapped()).isTrue();
        assertThat(splinter.isAttacking()).isTrue();
        assertThat(splinter.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    void normalCastingDoesNotEnterTappedOrAttacking() {
        harness.setHand(player1, List.of(new SplinterHamatoYoshi()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent splinter = findPermanent(player1, "Splinter, Hamato Yoshi");
        assertThat(splinter.isTapped()).isFalse();
        assertThat(splinter.isAttacking()).isFalse();
    }

    @Test
    void boostEndsWhenSplinterLeavesBattlefield() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterHamatoYoshi());
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new FootNinjas());
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(6);

        splinter.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Splinter, Hamato Yoshi");

        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(5);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new SplinterHamatoYoshi());
        addCreatureReady(player2, new FootNinjas());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new SplinterHamatoYoshi());
        Permanent first = addCreatureReady(player2, new FootNinjas());
        Permanent second = addCreatureReady(player2, new FootNinjas());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () ->
                gs.declareBlockers(gd, player2, List.of(
                        new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void sneakCannotReturnABlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new FootNinjas());
        addCreatureReady(player2, new FootNinjas());
        harness.setHand(player1, List.of(new SplinterHamatoYoshi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                    .isInstanceOf(IllegalStateException.class);
        });

        harness.assertInHand(player1, "Splinter, Hamato Yoshi");
        harness.assertOnBattlefield(player1, "Foot Ninjas");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sneakCannotBeCastDuringCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new FootNinjas());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new SplinterHamatoYoshi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Splinter, Hamato Yoshi");
        harness.assertOnBattlefield(player1, "Foot Ninjas");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sneakCannotReturnAnAttackerWhoseBlockerHasDied() {
        Permanent attacker = addCreatureReady(player1, new FootNinjas());
        Permanent blocker = addCreatureReady(player2, new FootNinjas());
        harness.setHand(player1, List.of(new SplinterHamatoYoshi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            blocker.setMarkedDamage(5);
            harness.runStateBasedActions();
            harness.assertInGraveyard(player2, "Foot Ninjas");

            assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                    .isInstanceOf(IllegalStateException.class);
        });

        harness.assertInHand(player1, "Splinter, Hamato Yoshi");
        harness.assertOnBattlefield(player1, "Foot Ninjas");
        assertThat(gd.stack).isEmpty();
    }
}
