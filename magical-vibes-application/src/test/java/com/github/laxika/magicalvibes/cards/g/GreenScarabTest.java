package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.FreyalisesWinds;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
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

@CardUsed({GreenScarab.class, BalduvianBears.class, FreyalisesWinds.class, KjeldoranWarrior.class})
class GreenScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a green creature")
    void cannotBeBlockedByGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new KjeldoranWarrior());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a non-green creature")
    void canBeBlockedByNonGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new KjeldoranWarrior());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new KjeldoranWarrior());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No boost when no opponent controls a green permanent")
    void noBoostWithoutOpponentGreenPermanent() {
        Permanent bears = addCreatureReady(player1, new KjeldoranWarrior());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent controls a green permanent")
    void boostedWhenOpponentControlsGreenPermanent() {
        Permanent bears = addCreatureReady(player1, new KjeldoranWarrior());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent controls a green noncreature permanent")
    void boostedWhenOpponentControlsGreenNoncreaturePermanent() {
        Permanent bears = addCreatureReady(player1, new KjeldoranWarrior());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new FreyalisesWinds());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Controller's own green permanent does not grant the boost")
    void ownGreenPermanentDoesNotBoost() {
        Permanent bears = addCreatureReady(player1, new KjeldoranWarrior());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    void boostEndsWhenOpponentGreenPermanentLeaves() {
        Permanent enchanted = addCreatureReady(player1, new KjeldoranWarrior());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(enchanted.getId());

        Permanent greenPermanent = harness.addToBattlefieldAndReturn(player2, new FreyalisesWinds());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId()).remove(greenPermanent);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(1);
    }

    @Test
    void onlyEnchantedCreatureGetsTheBoost() {
        Permanent enchanted = addCreatureReady(player1, new KjeldoranWarrior());
        Permanent other = addCreatureReady(player1, new KjeldoranWarrior());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenScarab());
        aura.setAttachedTo(enchanted.getId());

        harness.addToBattlefield(player2, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player1, new KjeldoranWarrior());
        harness.setHand(player1, List.of(new GreenScarab()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotEnchantNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new FreyalisesWinds());
        harness.setHand(player1, List.of(new GreenScarab()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
