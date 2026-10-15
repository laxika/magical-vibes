package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.Enslave;
import com.github.laxika.magicalvibes.cards.c.ChancellorOfTheDross;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulShadows.class, GrizzlyBears.class, Shock.class, SnappingSailback.class,
        LightningBolt.class, Enslave.class, ChancellorOfTheDross.class})
class SpitefulShadowsTest extends BaseCardTest {

    

    @Test
    @DisplayName("When enchanted creature is dealt non-combat damage, it deals that much to its controller")
    void spellDamageDealsEqualDamageToController() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpitefulShadows()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Spiteful Shadows"));

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 2);
    }

    @Test
    @DisplayName("Damage dealt to controller matches the amount of damage received")
    void damageAmountMatchesDamageReceived() {
        Permanent sailback = addCreatureReady(player2, new SnappingSailback());

        harness.setHand(player1, List.of(new SpitefulShadows()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, sailback.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castAndResolveInstant(player1, 0, sailbackId);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 3);
        harness.assertOnBattlefield(player2, "Snapping Sailback");
    }

    @Test
    @DisplayName("Damage goes to current controller, not original owner, when creature is stolen")
    void damageGoesToCurrentControllerWhenStolen() {
        Permanent sailback = addCreatureReady(player2, new SnappingSailback());

        harness.setHand(player1, List.of(new Enslave(), new SpitefulShadows()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castEnchantment(player1, 0, sailback.getId());
        harness.passBothPriorities();

        UUID stolenSailbackId = harness.getPermanentId(player1, "Snapping Sailback");
        harness.castEnchantment(player1, 0, stolenSailbackId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int ownerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, stolenSailbackId);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore);
    }

    @Test
    @DisplayName("No trigger when enchanted creature is not dealt damage")
    void noTriggerWithoutDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpitefulShadows()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The enchanted creature's lifelink offsets damage to its controller")
    void enchantedCreatureIsTheDamageSource() {
        Permanent chancellor = addCreatureReady(player2, new ChancellorOfTheDross());
        harness.setHand(player1, List.of(new SpitefulShadows()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, chancellor.getId());
        harness.passBothPriorities();

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int auraControllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, chancellor.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(auraControllerLifeBefore);
        harness.assertOnBattlefield(player2, "Chancellor of the Dross");
    }

    @Test
    @DisplayName("Each attached copy triggers even when the enchanted creature dies")
    void multipleCopiesTriggerOnLethalDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpitefulShadows(), new SpitefulShadows()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 4);
    }
}
