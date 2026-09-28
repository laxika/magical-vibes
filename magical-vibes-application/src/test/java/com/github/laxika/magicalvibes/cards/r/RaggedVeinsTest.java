package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlindWithAnger;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaggedVeins.class, BlindWithAnger.class, GlacialRay.class, RiverKaijin.class})
class RaggedVeinsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature dealt damage: its controller loses that much life")
    void controllerLosesLifeEqualToDamage() {
        Permanent kaijin = addCreatureReady(player2, new RiverKaijin());

        harness.setHand(player1, List.of(new RaggedVeins()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, kaijin.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID kaijinId = harness.getPermanentId(player2, "River Kaijin");
        harness.castInstant(player1, 0, kaijinId);
        harness.passBothPriorities(); // Resolve Glacial Ray - 2 damage
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertOnBattlefield(player2, "River Kaijin");
    }

    @Test
    @DisplayName("Life loss hits the creature's current controller, not its owner")
    void lifeLossHitsCurrentController() {
        Permanent kaijin = addCreatureReady(player2, new RiverKaijin());

        harness.setHand(player1, List.of(new RaggedVeins()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, kaijin.getId());
        harness.passBothPriorities();

        UUID kaijinId = harness.getPermanentId(player2, "River Kaijin");
        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, kaijinId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);

        int ownerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, kaijinId);
        harness.passBothPriorities(); // Resolve Glacial Ray
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore);
    }

    @Test
    @DisplayName("Life loss follows a control change before the trigger resolves")
    void lifeLossUsesControllerAtTriggerResolution() {
        Permanent kaijin = addCreatureReady(player2, new RiverKaijin());

        harness.setHand(player1, List.of(new RaggedVeins()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, kaijin.getId());
        harness.passBothPriorities();

        UUID kaijinId = harness.getPermanentId(player2, "River Kaijin");
        int ownerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, kaijinId);
        harness.passBothPriorities(); // Resolve Glacial Ray; Ragged Veins trigger remains on stack

        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, kaijinId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore);
    }

    @Test
    @DisplayName("No trigger while the enchanted creature takes no damage")
    void noTriggerWithoutDamage() {
        Permanent kaijin = addCreatureReady(player2, new RiverKaijin());

        harness.setHand(player1, List.of(new RaggedVeins()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, kaijin.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
