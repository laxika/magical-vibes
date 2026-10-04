package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreatureBond.class, GiantSpider.class, GrizzlyBears.class, Forest.class, HolyStrength.class,
        NevinyrralsDisk.class, SwordsToPlowshares.class, Unsummon.class})
class CreatureBondTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, its controller takes damage equal to its toughness")
    void enchantedCreatureDeathDealsDamageEqualToToughness() {
        // Giant Spider is 2/4 — this proves the damage tracks toughness (4), not power (2).
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new CreatureBond());
        bond.setAttachedTo(spider.getId());

        int lifeBefore = gd.getLife(player2.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("The aura's own controller takes the damage when it enchants their creature")
    void ownControllerTakesDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new CreatureBond());
        bond.setAttachedTo(bears.getId());

        int lifeBefore = gd.getLife(player1.getId());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Uses the enchanted creature's last-known effective toughness")
    void enchantedCreatureDeathUsesLastKnownEffectiveToughness() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent holyStrength = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        holyStrength.setAttachedTo(spider.getId());
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new CreatureBond());
        bond.setAttachedTo(spider.getId());

        int lifeBefore = gd.getLife(player2.getId());

        // Giant Spider is 2/4 and Holy Strength makes it 3/6. Lethal damage uses the effective
        // toughness, and Creature Bond should use that same last-known value after it dies.
        spider.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CreatureBond()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting the Aura attaches it and its death trigger survives the Aura leaving")
    void castAuraAndResolveDeathTrigger() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CreatureBond()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Creature Bond").getAttachedTo()).isEqualTo(spider.getId());

        int lifeBefore = gd.getLife(player2.getId());
        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(countPermanents(player1, "Creature Bond")).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Returning the enchanted creature to hand does not trigger damage")
    void bouncedCreatureDoesNotTriggerDamage() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new CreatureBond());
        bond.setAttachedTo(spider.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, spider.getId());

        assertThat(countPermanents(player2, "Giant Spider")).isZero();
        assertThat(gd.playerHands.get(player2.getId())).contains(spider.getCard());
        assertThat(countPermanents(player1, "Creature Bond")).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Exiling the enchanted creature does not trigger damage")
    void exiledCreatureDoesNotTriggerDamage() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new CreatureBond());
        bond.setAttachedTo(spider.getId());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, spider.getId());

        assertThat(countPermanents(player2, "Giant Spider")).isZero();
        assertThat(countPermanents(player1, "Creature Bond")).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("The death trigger fires when the Aura and enchanted creature are destroyed together")
    void simultaneousAuraAndCreatureDeathTriggersDamage() {
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new CreatureBond());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        bond.setAttachedTo(spider.getId());
        Permanent disk = harness.addToBattlefieldAndReturn(player1, new NevinyrralsDisk());
        disk.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Creature Bond")).isZero();
        assertThat(countPermanents(player1, "Giant Spider")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }
}
