package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StormFleetPyromancerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has raid-conditional ETB deal 2 damage to any target effect")
    void hasRaidEtbDamageEffect() {
        StormFleetPyromancer card = new StormFleetPyromancer();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isNull();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(RaidConditionalEffect.class);

        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(raid.wrapped()).isInstanceOf(DealDamageToAnyTargetEffect.class);

        DealDamageToAnyTargetEffect damage = (DealDamageToAnyTargetEffect) raid.wrapped();
        assertThat(damage.damage()).isEqualTo(2);
    }

    // ===== ETB with raid met — damage to creature =====

    @Test
    @DisplayName("ETB triggers and deals 2 damage to target creature when raid is met")
    void etbDeals2DamageToCreatureWithRaid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castStormFleetPyromancer(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Fleet Pyromancer");

        harness.passBothPriorities(); // resolve ETB trigger

        // 2 damage to a 2/2 kills it
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== ETB with raid met — damage to player =====

    @Test
    @DisplayName("ETB triggers and deals 2 damage to target player when raid is met")
    void etbDeals2DamageToPlayerWithRaid() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castStormFleetPyromancer(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== ETB without raid =====

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        harness.setLife(player2, 20);
        castStormFleetPyromancer(player2.getId());

        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Pyromancer"));

        // Opponent life unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Creature enters regardless =====

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersBattlefieldWithoutRaid() {
        castStormFleetPyromancer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Pyromancer"));
    }

    // ===== Raid lost before resolution (intervening-if) =====

    @Test
    @DisplayName("ETB does nothing if raid condition is lost before resolution")
    void etbFizzlesWhenRaidLost() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castStormFleetPyromancer(player2.getId());

        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove the raid flag before ETB resolves
        gd.playersDeclaredAttackersThisTurn.clear();

        harness.passBothPriorities(); // resolve ETB trigger — raid no longer met

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("raid ability does nothing"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castStormFleetPyromancer(targetId);

        harness.passBothPriorities(); // resolve creature spell — ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castStormFleetPyromancer(UUID targetId) {
        harness.setHand(player1, List.of(new StormFleetPyromancer()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }
}
