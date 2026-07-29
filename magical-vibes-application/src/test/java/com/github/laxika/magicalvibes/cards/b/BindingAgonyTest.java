package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SnappingSailback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BindingAgonyTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage to enchanted creature deals that much to its controller")
    void spellDamageDealsEqualDamageToController() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Grizzly Bears

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Binding Agony"));

        while (gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Binding Agony"))) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 2);
    }

    @Test
    @DisplayName("Damage dealt to controller matches the amount of damage received")
    void damageAmountMatchesDamageReceived() {
        Permanent sailback = addCreatureReady(player2, new SnappingSailback());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, sailback.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve Lightning Bolt — 3 damage

        while (gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Binding Agony"))) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 3);
        harness.assertOnBattlefield(player2, "Snapping Sailback");
    }

    @Test
    @DisplayName("No trigger when the enchanted creature is not dealt damage")
    void noTriggerWithoutDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
