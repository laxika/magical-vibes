package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.o.OpalineUnicorn;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VipersKiss.class, BurnishedHart.class, NessianCourser.class, OpalineUnicorn.class, TravelersAmulet.class})
class VipersKissTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1")
    void enchantedCreatureGetsMinusOneMinusOne() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());

        harness.setHand(player1, List.of(new VipersKiss()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        harness.setHand(player1, List.of(new VipersKiss()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet());

        harness.setHand(player1, List.of(new VipersKiss()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate mana abilities")
    void enchantedCreatureCannotActivateManaAbilities() {
        Permanent creature = addCreatureReady(player1, new OpalineUnicorn());
        harness.setHand(player1, List.of(new VipersKiss()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple Kisses stack and put a zero-toughness creature and its Auras into graveyards")
    void multipleKissesCanKillCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OpalineUnicorn());
        harness.setHand(player1, List.of(new VipersKiss(), new VipersKiss()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof OpalineUnicorn);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof VipersKiss).hasSize(2);
    }
}
