package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.DwarvenArmory;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedScarab.class, BalduvianBears.class, BalduvianBarbarians.class, DwarvenArmory.class})
class RedScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new BalduvianBarbarians());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a non-red creature")
    void canBeBlockedByNonRedCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No boost when no opponent controls a red permanent")
    void noBoostWithoutOpponentRedPermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent controls a red permanent")
    void boostedWhenOpponentControlsRedPermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new BalduvianBarbarians());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent controls a red noncreature permanent")
    void boostedWhenOpponentControlsRedNonCreaturePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new DwarvenArmory());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Controller's own red permanent does not grant the boost")
    void ownRedPermanentDoesNotBoost() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new BalduvianBarbarians());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-red opponent permanent does not grant the boost")
    void nonRedOpponentPermanentDoesNotBoost() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when the opponent's red permanent leaves")
    void boostEndsWhenOpponentRedPermanentLeaves() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(bears.getId());

        Permanent redPermanent = harness.addToBattlefieldAndReturn(player2, new BalduvianBarbarians());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(redPermanent);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the enchanted creature gets the boost")
    void onlyEnchantedCreatureGetsBoost() {
        Permanent enchanted = addCreatureReady(player1, new BalduvianBears());
        Permanent other = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(enchanted.getId());

        harness.addToBattlefield(player2, new BalduvianBarbarians());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost uses the Aura controller's opponents")
    void boostUsesAuraControllerForOpponentCheck() {
        Permanent enchanted = addCreatureReady(player2, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedScarab());
        aura.setAttachedTo(enchanted.getId());

        harness.addToBattlefield(player2, new BalduvianBarbarians());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
    }

    @Test
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new RedScarab()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotEnchantNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new DwarvenArmory());
        harness.setHand(player1, List.of(new RedScarab()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
