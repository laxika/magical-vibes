package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.Enslave;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Grizzly Bears

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Spiteful Shadows"));

        while (gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Spiteful Shadows"))) {
            harness.passBothPriorities();
        }

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
        harness.castInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve Lightning Bolt — 3 damage

        while (gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Spiteful Shadows"))) {
            harness.passBothPriorities();
        }

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

        harness.castInstant(player1, 0, stolenSailbackId);
        harness.passBothPriorities(); // Resolve Shock
        while (gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Spiteful Shadows"))) {
            harness.passBothPriorities();
        }

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
}
