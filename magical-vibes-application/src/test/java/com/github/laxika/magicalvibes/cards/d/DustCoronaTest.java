package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({DustCorona.class, GossamerPhantasm.class, ProdigalPyromancer.class})
class DustCoronaTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+0")
    void enchantedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent unenchantedCreature = addCreatureReady(player1, new ProdigalPyromancer());
        attachDustCorona(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, unenchantedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature with flying")
    void cannotBeBlockedByFlyingCreature() {
        Permanent attacker = addAttacker();
        attachDustCorona(attacker);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a creature without flying")
    void canBeBlockedByNonFlyingCreature() {
        Permanent attacker = addAttacker();
        attachDustCorona(attacker);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Dust Corona can enchant only a creature")
    void cannotEnchantNonCreaturePermanent() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new DustCorona());
        harness.setHand(player1, List.of(new DustCorona()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Dust Corona can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new DustCorona()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dust Corona");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dust Corona does not prevent flying creatures from blocking other attackers")
    void flyingCreatureCanBlockUnenchantedAttacker() {
        Permanent enchantedAttacker = addAttacker();
        attachDustCorona(enchantedAttacker);
        Permanent unenchantedAttacker = addAttacker();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());

        prepareDeclareBlockers();
        declareBlock(blocker, unenchantedAttacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new ProdigalPyromancer());
        attacker.setAttacking(true);
        return attacker;
    }

    private void attachDustCorona(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DustCorona());
        aura.setAttachedTo(creature.getId());
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
