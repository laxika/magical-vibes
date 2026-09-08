package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Iceberg;
import com.github.laxika.magicalvibes.cards.i.IllusionaryForces;
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

@CardUsed({BlueScarab.class, BalduvianBears.class, Iceberg.class, IllusionaryForces.class})
class BlueScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a blue creature")
    void cannotBeBlockedByBlueCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new IllusionaryForces());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a non-blue creature")
    void canBeBlockedByNonBlueCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No boost when no opponent controls a blue permanent")
    void noBoostWithoutOpponentBluePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent controls a blue permanent")
    void boostedWhenOpponentControlsBluePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new IllusionaryForces());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Controller's own blue permanent does not grant the boost")
    void ownBluePermanentDoesNotBoost() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new Iceberg());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void onlyEnchantedCreatureGetsTheBoost() {
        Permanent enchanted = addCreatureReady(player1, new BalduvianBears());
        Permanent other = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(enchanted.getId());

        harness.addToBattlefield(player2, new Iceberg());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void boostedWhenOpponentControlsBlueNonCreaturePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new Iceberg());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void boostEndsWhenOpponentBluePermanentLeaves() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueScarab());
        aura.setAttachedTo(bears.getId());

        Permanent iceberg = harness.addToBattlefieldAndReturn(player2, new Iceberg());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(iceberg);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void canEnchantCreature() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new BlueScarab()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotEnchantNonCreature() {
        Permanent iceberg = harness.addToBattlefieldAndReturn(player1, new Iceberg());
        harness.setHand(player1, List.of(new BlueScarab()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, iceberg.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
