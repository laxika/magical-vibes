package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondWeapon.class, AirElemental.class, Forest.class, GrizzlyBears.class, Shock.class})
class DiamondWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1} less for each permanent card in its controller's graveyard")
    void costReductionCountsPermanentCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new DiamondWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Nonpermanent cards do not reduce its cost")
    void costReductionIgnoresNonpermanentCards() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new DiamondWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Combat damage to Diamond Weapon is prevented")
    void combatDamageToDiamondWeaponIsPrevented() {
        Permanent diamondWeapon = addCreatureReady(player1, new DiamondWeapon());
        diamondWeapon.setBlocking(true);
        diamondWeapon.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AirElemental());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(diamondWeapon);
        assertThat(diamondWeapon.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card instanceof AirElemental);
    }

    @Test
    @DisplayName("Opponent's graveyard does not reduce the cost")
    void costReductionIgnoresOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DiamondWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Land cards also reduce the casting cost")
    void landCardsReduceCastingCost() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DiamondWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess reduction still requires both green mana")
    void excessReductionDoesNotReduceColoredMana() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DiamondWeapon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof DiamondWeapon);
    }

    @Test
    @DisplayName("Combat damage from blockers is prevented while Diamond Weapon deals damage")
    void attackingDiamondWeaponPreventsOnlyIncomingDamage() {
        Permanent diamondWeapon = addCreatureReady(player1, new DiamondWeapon());
        diamondWeapon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(diamondWeapon);
        assertThat(diamondWeapon.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Noncombat damage to Diamond Weapon is not prevented")
    void noncombatDamageToDiamondWeaponIsNotPrevented() {
        Permanent diamondWeapon = addCreatureReady(player2, new DiamondWeapon());
        UUID diamondWeaponId = diamondWeapon.getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, diamondWeaponId);
        harness.passBothPriorities();

        assertThat(diamondWeapon.getMarkedDamage()).isEqualTo(2);
    }
}
