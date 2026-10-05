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
        harness.castAndResolveInstant(player1, 0, kaijinId);
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
        harness.castAndResolveInstant(player1, 0, kaijinId);

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);

        int ownerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, kaijinId);
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
        harness.castAndResolveInstant(player1, 0, kaijinId); // Ragged Veins trigger remains on stack

        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, kaijinId);
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

    @Test
    @DisplayName("Life loss uses the creature's last controller when it dies after changing control")
    void lifeLossUsesLastControllerAfterCreatureLeaves() {
        Permanent kaijin = addCreatureReady(player2, new RiverKaijin());
        UUID kaijinId = kaijin.getId();
        harness.setHand(player1, List.of(new RaggedVeins()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, kaijinId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, kaijinId);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, kaijinId);
        harness.assertOnBattlefield(player1, "River Kaijin");
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, kaijinId);
        harness.assertInGraveyard(player2, "River Kaijin");
        harness.assertInGraveyard(player1, "Ragged Veins");
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Flash allows Ragged Veins to resolve before pending damage")
    void flashAuraTriggersForPendingDamage() {
        Permanent kaijin = addCreatureReady(player2, new RiverKaijin());
        harness.setHand(player1, List.of(new GlacialRay(), new RaggedVeins()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, kaijin.getId());
        harness.castEnchantment(player1, 0, kaijin.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ragged Veins");
        harness.assertLife(player2, 20);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

}
