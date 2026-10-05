package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfernoFist.class, RuneclawBear.class, ChandraPyromaster.class})
class InfernoFistTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Inferno Fist attaches it to the targeted creature you control")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new InfernoFist()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inferno Fist")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+0")
    void enchantedCreatureGetsBoost() {
        Permanent bears = attachFistTo(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to base stats when Inferno Fist leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = attachFistTo(player1);
        Permanent aura = findPermanent(player1, "Inferno Fist");

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("{R}, Sacrifice: deals 2 damage to a target player")
    void sacrificeDealsDamageToPlayer() {
        attachFistTo(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Inferno Fist");
        harness.assertInGraveyard(player1, "Inferno Fist");
    }

    @Test
    @DisplayName("{R}, Sacrifice: deals 2 damage to a target creature, killing it")
    void sacrificeDealsDamageToCreature() {
        Permanent own = attachFistTo(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.activateAbility(player1, 1, 0, null, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Sacrificing the Aura removes the +2/+0 from the enchanted creature")
    void sacrificeRemovesBoost() {
        Permanent bears = attachFistTo(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a creature an opponent controls")
    void cannotEnchantOpponentCreature() {
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new InfernoFist()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeDamageResolves() {
        Permanent bear = attachFistTo(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Inferno Fist");
        harness.assertInGraveyard(player1, "Inferno Fist");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void sacrificeCanDamageItsController() {
        attachFistTo(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificeCanDamageTheEnchantedCreature() {
        Permanent bear = attachFistTo(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Inferno Fist");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void sacrificeCanDamageAPlaneswalker() {
        attachFistTo(player1);
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Chandra, Pyromaster");
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({InvasionOfZendikar.class})
    void sacrificeCanDamageABattle() {
        attachFistTo(player1);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
        harness.assertInGraveyard(player1, "Inferno Fist");
    }

    @Test
    void cannotActivateWithoutRedMana() {
        attachFistTo(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Inferno Fist");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotEnchantANoncreaturePermanent() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new InfernoFist()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, chandra.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void auraDoesNotResolveWhenItsTargetLeavesTheBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new InfernoFist()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, bear.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerGraveyards.get(player1.getId()).add(bear.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Inferno Fist");
        harness.assertInGraveyard(player1, "Inferno Fist");
    }

    @Test
    void auraGoesToGraveyardWhenOpponentGainsControlOfEnchantedCreature() {
        Permanent bear = attachFistTo(player1);

        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Inferno Fist");
        harness.assertInGraveyard(player1, "Inferno Fist");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    private Permanent attachFistTo(Player controller) {
        Permanent bears = harness.addToBattlefieldAndReturn(controller, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(controller, new InfernoFist());
        aura.setAttachedTo(bears.getId());
        return bears;
    }
}
