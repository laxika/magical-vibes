package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fungusaur.class, FugitiveWizard.class, GiantGrowth.class, Shock.class})
class FungusaurTest extends BaseCardTest {

    @Test
    @DisplayName("When Fungusaur takes non-lethal combat damage, it gets a +1/+1 counter and survives")
    void nonLethalCombatDamageAddsCounter() {
        addCreatureReady(player2, new Fungusaur());
        addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player1);
        resolveAllTriggers();

        // Fungusaur survives (2 toughness, 1 damage) and has a +1/+1 counter
        harness.assertOnBattlefield(player2, "Fungusaur");
        Permanent survivor = findPermanent(player2, "Fungusaur");
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Fugitive Wizard dies to the 2/2 Fungusaur
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("When Fungusaur takes non-lethal noncombat damage, it gets a +1/+1 counter")
    void nonLethalNoncombatDamageAddsCounter() {
        Permanent fungusaur = addCreatureReady(player2, new Fungusaur());
        harness.setHand(player1, List.of(new GiantGrowth(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID fungusaurId = fungusaur.getId();
        harness.castInstant(player1, 0, fungusaurId);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, fungusaurId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(fungusaur);
        assertThat(fungusaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fungusaur triggers only once when multiple creatures damage it simultaneously")
    void multipleCombatDamageSourcesCreateOnlyOneTrigger() {
        Permanent fungusaur = addCreatureReady(player1, new Fungusaur());
        Permanent firstWizard = addCreatureReady(player2, new FugitiveWizard());
        Permanent secondWizard = addCreatureReady(player2, new FugitiveWizard());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID fungusaurId = fungusaur.getId();
        harness.castInstant(player1, 0, fungusaurId);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, fungusaur)).isEqualTo(5);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstWizard.getId(), 1,
                secondWizard.getId(), 4));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fungusaur);
        resolveAllTriggers();

        assertThat(fungusaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lethal damage destroys Fungusaur before the +1/+1 counter trigger can resolve")
    void lethalDamageKillsBeforeCounterResolves() {
        harness.addToBattlefield(player2, new Fungusaur()); // 2/2
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID fungusaurId = harness.getPermanentId(player2, "Fungusaur");
        harness.castInstant(player1, 0, fungusaurId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage, lethal for a 2/2

        // The trigger is on the stack, but state-based actions already destroyed Fungusaur
        harness.assertInGraveyard(player2, "Fungusaur");

        // Resolving the trigger does nothing (source is gone)
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Fungusaur");
    }
}
