package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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
        DrudgeSkeletons.class, MonssGoblinRaiders.class, GrizzlyBears.class, LightningBolt.class})
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
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
