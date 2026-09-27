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

@CardUsed({FirstVolley.class, GnarledMass.class, MendingHands.class})
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

        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        // 3 combat damage fully prevented (shield 4 >= 3) → life unchanged, 1 of shield remains
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention shields are cleared at end of turn")
    void preventionShieldsClearedAtEndOfTurn() {
        Permanent perm = addCreatureReady(player1, new GnarledMass());
        perm.setDamagePreventionShield(4);
        gd.playerDamagePreventionShields.put(player1.getId(), 4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent afterCleanup = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(afterCleanup.getDamagePreventionShield()).isEqualTo(0);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }
}
