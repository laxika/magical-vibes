package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.c.Compost;
import com.github.laxika.magicalvibes.cards.s.SigilOfSleep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MultanisDecree.class, Compost.class, MetathranSoldier.class, SigilOfSleep.class})
class MultanisDecreeTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all enchantments and gains 2 life per destroyed enchantment")
    void destroysAllEnchantmentsAndGainsLifePerDestroyedEnchantment() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new Compost());
        harness.addToBattlefield(player1, new MetathranSoldier());
        harness.setHand(player1, List.of(new MultanisDecree()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Compost");
        harness.assertNotOnBattlefield(player2, "Compost");
        harness.assertOnBattlefield(player1, "Metathran Soldier");
        harness.assertLife(player1, STARTING_LIFE + 4);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Gains no life when no enchantments are destroyed")
    void gainsNoLifeWhenNoEnchantmentsAreDestroyed() {
        harness.addToBattlefield(player1, new MetathranSoldier());
        harness.setHand(player1, List.of(new MultanisDecree()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, STARTING_LIFE);
        harness.assertOnBattlefield(player1, "Metathran Soldier");
    }

    @Test
    @DisplayName("Counts an attached Aura and leaves its enchanted creature on the battlefield")
    void destroysAttachedAuraAndCountsItForLifeGain() {
        var creature = harness.addToBattlefieldAndReturn(player2, new MetathranSoldier());
        harness.setHand(player1, List.of(new SigilOfSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sigil of Sleep");

        harness.addToBattlefield(player1, new Compost());
        harness.setHand(player1, List.of(new MultanisDecree()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Compost");
        harness.assertInGraveyard(player1, "Sigil of Sleep");
        harness.assertOnBattlefield(player2, "Metathran Soldier");
        harness.assertLife(player1, STARTING_LIFE + 4);
        harness.assertLife(player2, STARTING_LIFE);
    }
}
