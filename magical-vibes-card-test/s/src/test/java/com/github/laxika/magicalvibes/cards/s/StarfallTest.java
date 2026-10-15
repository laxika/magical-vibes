package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.cards.h.HarvestguardAlseids;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.cards.n.NyxFleeceRam;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Starfall.class, NyxFleeceRam.class, PensiveMinotaur.class,
        FontOfFertility.class, HarvestguardAlseids.class, Hubris.class})
class StarfallTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to an enchantment creature and its controller")
    void dealsDamageToEnchantmentCreatureAndController() {
        harness.addToBattlefield(player2, new NyxFleeceRam());
        harness.setHand(player1, List.of(new Starfall()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Nyx-Fleece Ram");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Nyx-Fleece Ram");
        assertThat(findPermanent(player2, "Nyx-Fleece Ram").getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not damage the controller of a non-enchantment creature")
    void doesNotDamageNonEnchantmentCreatureController() {
        harness.addToBattlefield(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new Starfall()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Pensive Minotaur");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Pensive Minotaur");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new FontOfFertility());
        harness.setHand(player1, List.of(new Starfall()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Font of Fertility");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal damage to an enchantment creature still damages its controller")
    void lethalDamageStillDamagesController() {
        harness.addToBattlefield(player2, new HarvestguardAlseids());
        harness.setHand(player1, List.of(new Starfall()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Harvestguard Alseids"));

        harness.assertInGraveyard(player2, "Harvestguard Alseids");
        harness.assertNotOnBattlefield(player2, "Harvestguard Alseids");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Targeting your own enchantment creature damages you")
    void damagesOwnEnchantmentCreatureController() {
        harness.addToBattlefield(player1, new NyxFleeceRam());
        harness.setHand(player1, List.of(new Starfall()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Nyx-Fleece Ram"));

        assertThat(findPermanent(player1, "Nyx-Fleece Ram").getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An enchantment creature returned to hand makes the spell fail to resolve")
    void removedTargetDoesNotDamageController() {
        harness.addToBattlefield(player2, new NyxFleeceRam());
        harness.setHand(player1, List.of(new Starfall()));
        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Nyx-Fleece Ram");
        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Nyx-Fleece Ram");
        harness.assertInGraveyard(player1, "Starfall");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Starfall()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
