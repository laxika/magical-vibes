package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cloudthresher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SonicTheHedgehog.class, Cloudthresher.class, GrizzlyBears.class, Shock.class})
class SonicTheHedgehogTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts counters on controlled creatures with flash or haste")
    void attackCountersFlashOrHasteCreatures() {
        Permanent sonic = addCreatureReady(player1, new SonicTheHedgehog());
        Permanent flashCreature = addCreatureReady(player1, new Cloudthresher());
        Permanent vanillaCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(sonic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flashCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vanillaCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage to a controlled creature with flash or haste creates a tapped Treasure")
    void damageToFlashCreatureCreatesTappedTreasure() {
        harness.addToBattlefield(player1, new SonicTheHedgehog());
        Permanent flashCreature = addCreatureReady(player1, new Cloudthresher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, flashCreature.getId());
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Damage to a controlled creature without flash or haste does not create a Treasure")
    void damageToCreatureWithoutFlashOrHasteDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new SonicTheHedgehog());
        Permanent vanillaCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, vanillaCreature.getId());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking also counters nonattacking qualifying creatures but not opposing creatures")
    void attackCountersNonattackingCreaturesOnlyOnOwnBattlefield() {
        Permanent sonic = addCreatureReady(player1, new SonicTheHedgehog());
        Permanent flashCreature = addCreatureReady(player1, new Cloudthresher());
        Permanent opposingFlashCreature = addCreatureReady(player2, new Cloudthresher());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(sonic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flashCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingFlashCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each separate damage event to Sonic creates one tapped Treasure regardless of damage amount")
    void repeatedDamageToSonicCreatesOneTreasurePerEvent() {
        Permanent sonic = addCreatureReady(player1, new SonicTheHedgehog());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, sonic.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.castAndResolveInstant(player2, 0, sonic.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sonic.getCard());
        assertThat(findPermanents(player1, "Treasure")).hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Damage to an opposing creature with flash creates no Treasure")
    void opposingFlashCreatureDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new SonicTheHedgehog());
        Permanent flashCreature = addCreatureReady(player2, new Cloudthresher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, flashCreature.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Lethal combat damage to a controlled flash creature still creates a Treasure")
    void lethalCombatDamageToFlashCreatureCreatesTreasure() {
        harness.addToBattlefield(player1, new SonicTheHedgehog());
        Permanent attacker = addCreatureReady(player1, new Cloudthresher());
        addCreatureReady(player2, new Cloudthresher());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(findPermanents(player1, "Treasure")).hasSize(1).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Sonic creates a Treasure for lethal combat damage to himself while blocking")
    void sonicDyingInCombatStillCreatesTreasure() {
        Permanent sonic = addCreatureReady(player1, new SonicTheHedgehog());
        addCreatureReady(player2, new Cloudthresher());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sonic.getCard());
        assertThat(findPermanents(player1, "Treasure")).hasSize(1).allMatch(Permanent::isTapped);
    }
}
