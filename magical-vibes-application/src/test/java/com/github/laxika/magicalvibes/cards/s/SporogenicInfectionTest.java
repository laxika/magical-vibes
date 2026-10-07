package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraFinesse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({SporogenicInfection.class, GrizzlyBears.class, HillGiant.class, Shock.class, AuraFinesse.class})
class SporogenicInfectionTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability sacrifices another creature, not the enchanted creature")
    void sacrificesAnotherCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());

        castSporogenicInfection(enchanted, player2.getId());

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Sporogenic Infection");
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability does nothing when the enchanted creature is the only creature")
    void doesNotSacrificeEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());

        castSporogenicInfection(enchanted, player2.getId());

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Sporogenic Infection");
    }

    @Test
    @DisplayName("Damage to the enchanted creature destroys it")
    void damageDestroysEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        castSporogenicInfection(enchanted, player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID enchantedId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, enchantedId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Sporogenic Infection");
    }

    @Test
    void targetedPlayerChoosesWhichOtherCreatureToSacrifice() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent spared = addCreatureReady(player2, new GrizzlyBears());

        castSporogenicInfection(enchanted, player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));
        resolveAllTriggers();

        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(enchanted, spared).doesNotContain(chosen);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canMakeAuraControllerSacrificeTheirCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        addCreatureReady(player1, new GrizzlyBears());

        castSporogenicInfection(enchanted, player1.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Sporogenic Infection");
    }

    @Test
    void movingAuraAfterDamageStillDestroysDamagedCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent destination = addCreatureReady(player2, new GrizzlyBears());
        castSporogenicInfection(enchanted, player1.getId());
        UUID auraId = harness.getPermanentId(player1, "Sporogenic Infection");

        harness.setHand(player1, List.of(new Shock(), new AuraFinesse()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, enchanted.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, List.of(auraId, destination.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Sporogenic Infection");
    }

    private void castSporogenicInfection(Permanent enchanted, UUID playerTargetId) {
        harness.setHand(player1, List.of(new SporogenicInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, List.of(enchanted.getId(), playerTargetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
