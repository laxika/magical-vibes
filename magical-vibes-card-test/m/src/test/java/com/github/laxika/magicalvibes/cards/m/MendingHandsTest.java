package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;

@CardUsed({MendingHands.class, GrizzlyBears.class, FirstVolley.class, GnarledMass.class})
class MendingHandsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mending Hands adds a 4-damage prevention shield to target creature")
    void resolvingAddsCreaturePrevention() {
        Permanent target = addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getDamagePreventionShield()).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolving Mending Hands targeting a player adds a 4-damage prevention shield")
    void resolvingAddsPlayerPrevention() {
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mending Hands prevents exactly the next 4 damage to a target creature")
    void preventsNextFourDamageToTargetCreature() {
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player1, List.of(
                new FirstVolley(), new FirstVolley(), new FirstVolley(), new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 8);
        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player1, 0, target.getId());
        }

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Player prevention shield reduces unblocked combat damage")
    void playerPreventionReducesCombatDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().playerDamagePreventionShields.put(player2.getId(), 4);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        // Two combat damage is fully prevented; two shield points remain.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevention shields are cleared at end of turn")
    void preventionShieldsClearedAtEndOfTurn() {
        Permanent perm = addCreatureReady(player1, new GrizzlyBears());
        perm.setDamagePreventionShield(4);
        harness.getGameData().playerDamagePreventionShields.put(player1.getId(), 4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent afterCleanup = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(afterCleanup.getDamagePreventionShield()).isEqualTo(0);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Damage beyond the fourth point reaches the protected creature")
    void damageAfterShieldIsExhaustedReachesCreature() {
        Permanent target = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        for (int i = 0; i < 5; i++) {
            harness.setHand(player1, List.of(new FirstVolley()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castAndResolveInstant(player1, 0, target.getId());
        }

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.assertOnBattlefield(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Two Mending Hands prevent eight damage across successive sources")
    void multipleShieldsAccumulate() {
        Permanent target = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands(), new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        for (int i = 0; i < 9; i++) {
            harness.setHand(player1, List.of(new FirstVolley()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castAndResolveInstant(player1, 0, target.getId());
        }

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Mending Hands prevents four spell damage to a player without protecting their creatures")
    void playerShieldPreventsOnlyFirstFourSpellDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        for (int i = 0; i < 5; i++) {
            Permanent target = addCreatureReady(player2, new GnarledMass());
            harness.setHand(player1, List.of(new FirstVolley()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castAndResolveInstant(player1, 0, target.getId());
            assertThat(target.getMarkedDamage()).isEqualTo(1);
            harness.assertLife(player2, i < 4 ? 20 : 19);
        }
    }

    @Test
    @DisplayName("Shields created by resolving Mending Hands expire at cleanup")
    void resolvedShieldsExpireAtCleanup() {
        Permanent target = addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands(), new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Resolving Mending Hands prevents unblocked combat damage to the chosen player")
    void resolvedPlayerShieldPreventsCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
