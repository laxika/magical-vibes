package com.github.laxika.magicalvibes.cards.s;

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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SporogenicInfection.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class SporogenicInfectionTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability sacrifices another creature, not the enchanted creature")
    void sacrificesAnotherCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());

        castSporogenicInfection(enchanted, player2.getId());

        assertThat(onBattlefield(player2, "Hill Giant")).isTrue();
        assertThat(onBattlefield(player2, "Grizzly Bears")).isFalse();
        assertThat(onBattlefield(player1, "Sporogenic Infection")).isTrue();
        assertThat(other).isNotNull();
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability does nothing when the enchanted creature is the only creature")
    void doesNotSacrificeEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());

        castSporogenicInfection(enchanted, player2.getId());

        assertThat(onBattlefield(player2, "Hill Giant")).isTrue();
        assertThat(onBattlefield(player1, "Sporogenic Infection")).isTrue();
    }

    @Test
    @DisplayName("Damage to the enchanted creature destroys it")
    void damageDestroysEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        castSporogenicInfection(enchanted, player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID enchantedId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, enchantedId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(onBattlefield(player2, "Hill Giant")).isFalse();
        assertThat(onBattlefield(player1, "Sporogenic Infection")).isFalse();
    }

    private void castSporogenicInfection(Permanent enchanted, UUID playerTargetId) {
        harness.setHand(player1, List.of(new SporogenicInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, List.of(enchanted.getId(), playerTargetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private boolean onBattlefield(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals(name));
    }
}
