package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfernoFistTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Inferno Fist attaches it to the targeted creature you control")
    void resolvingAttachesToTarget() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

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
        Permanent bears = attachFistTo(player1.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to base stats when Inferno Fist leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = attachFistTo(player1.getId());
        Permanent aura = findPermanent(player1, "Inferno Fist");

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("{R}, Sacrifice: deals 2 damage to a target player")
    void sacrificeDealsDamageToPlayer() {
        attachFistTo(player1.getId());
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
        Permanent own = attachFistTo(player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, 0, null, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing the Aura removes the +2/+0 from the enchanted creature")
    void sacrificeRemovesBoost() {
        Permanent bears = attachFistTo(player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a creature an opponent controls")
    void cannotEnchantOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID enemyBears = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new InfernoFist()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enemyBears))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachFistTo(UUID controllerId) {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(controllerId).add(bears);

        Permanent aura = new Permanent(new InfernoFist());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(controllerId).add(aura);
        return bears;
    }
}
