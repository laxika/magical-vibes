package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FinalDeath;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({NexusWardens.class, UnderworldDreams.class, NyxbornColossus.class, FinalDeath.class})
class NexusWardensTest extends BaseCardTest {

    @Test
    @DisplayName("You gain 2 life when an enchantment you control enters")
    void gainsLifeWhenYourEnchantmentEnters() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player1, List.of(new UnderworldDreams()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An opponent's enchantment does not trigger it")
    void doesNotTriggerForOpponentsEnchantment() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player2, List.of(new UnderworldDreams()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A creature entering does not trigger it")
    void doesNotTriggerForCreature() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player1, List.of(new NexusWardens()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An enchantment creature triggers life gain only after entering")
    void enchantmentCreatureTriggersAfterEntering() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Nexus Wardens triggers separately for the same enchantment")
    void multipleWardensEachGainLife() {
        addCreatureReady(player1, new NexusWardens());
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player1, List.of(new UnderworldDreams()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each enchantment entering in the same turn gains 2 life")
    void triggersForEachEnchantmentInTheSameTurn() {
        addCreatureReady(player1, new NexusWardens());
        harness.setHand(player1, List.of(new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 12);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain still resolves after Nexus Wardens leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        addCreatureReady(player1, new NexusWardens());
        var wardensId = harness.getPermanentId(player1, "Nexus Wardens");
        harness.setHand(player1, List.of(new UnderworldDreams()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player2, 0, wardensId);
        harness.assertNotOnBattlefield(player1, "Nexus Wardens");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
