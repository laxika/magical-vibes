package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarKnight;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SpiritOfResistance.class, SavannahLions.class, MerfolkOfThePearlTrident.class,
        DrudgeSkeletons.class, MonssGoblinRaiders.class, GrizzlyBears.class, LightningBolt.class, LlanowarKnight.class, ShivanZombie.class})
class SpiritOfResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage to its controller while they control a permanent of each color")
    void preventsDamageWhenControllerHasAllColors() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new MonssGoblinRaiders());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not prevent damage when the controller lacks one of the colors")
    void doesNotPreventDamageWhenControllerLacksAColor() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Prevents combat damage to its controller while the condition is met")
    void preventsCombatDamageWhenControllerHasAllColors() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new MonssGoblinRaiders());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Multicolored permanents can satisfy several colors at once")
    void multicoloredPermanentsSatisfySeveralColors() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new LlanowarKnight());
        harness.addToBattlefield(player1, new ShivanZombie());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The enchantment itself satisfies the white requirement")
    void enchantmentCountsAsWhitePermanent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new MonssGoblinRaiders());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's permanent cannot satisfy a missing color")
    void opponentsPermanentDoesNotSupplyMissingColor() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new LlanowarKnight());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new ShivanZombie());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Prevention does not protect creatures and stops when a required permanent dies")
    void preventionStopsAfterRequiredPermanentDies() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new LlanowarKnight());
        harness.addToBattlefield(player1, new ShivanZombie());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Shivan Zombie"));
        harness.assertInGraveyard(player1, "Shivan Zombie");
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Only the enchantment's controller is protected")
    void doesNotPreventDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SpiritOfResistance());
        harness.addToBattlefield(player1, new LlanowarKnight());
        harness.addToBattlefield(player1, new ShivanZombie());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }
}
