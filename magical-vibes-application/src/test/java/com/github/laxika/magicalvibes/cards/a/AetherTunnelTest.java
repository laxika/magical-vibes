package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AetherTunnel.class, GrizzlyBears.class, GloriousAnthem.class})
class AetherTunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Aether Tunnel attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new AetherTunnel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aether Tunnel")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Aether Tunnel targeting a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new AetherTunnel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, anthem.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+0")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AetherTunnel());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked")
    void enchantedCreatureCantBeBlocked() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AetherTunnel());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unenchanted creature can still be blocked")
    void withoutAuraCreatureCanBeBlocked() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Aether Tunnel can enchant an opponent's creature and affects only that creature")
    void enchantsOpponentsCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AetherTunnel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, enchanted)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, other)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, own)).isFalse();
    }

    @Test
    @DisplayName("Removing Aether Tunnel ends both continuous effects")
    void removingAuraEndsBothEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AetherTunnel());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }
}
