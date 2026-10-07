package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArachnoidAdaptation;
import com.github.laxika.magicalvibes.cards.d.DeadlyDerision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IridescentBlademaster;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TributeToTheWorldTree.class, GrizzlyBears.class, HillGiant.class,
        IridescentBlademaster.class, ArachnoidAdaptation.class, DeadlyDerision.class, Naturalize.class})
class TributeToTheWorldTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for an entering creature with power 3 or greater")
    void drawsForCreatureWithPowerAtLeastThree() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on an entering creature with power less than 3")
    void putsCountersOnCreatureBelowPowerThree() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Checks power when each trigger resolves")
    void checksPowerAtResolution() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's entering creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());

        Permanent creature = harness.enterBattlefieldAndReturn(player2, new IridescentBlademaster());

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draws instead of adding counters when an instant raises power before resolution")
    void drawsAfterPowerIncreasesInResponse() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.setLibrary(player1, List.of(new IridescentBlademaster()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new IridescentBlademaster());
        harness.setHand(player1, List.of(new ArachnoidAdaptation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draws for a creature that left with power at least three")
    void drawsForRemovedLargeCreature() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.setLibrary(player1, List.of(new IridescentBlademaster()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new DeadlyDerision()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Uses increased power just before the entering creature leaves")
    void usesLastKnownPowerAfterPumpAndRemoval() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.setLibrary(player1, List.of(new IridescentBlademaster()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new IridescentBlademaster());
        harness.setHand(player1, List.of(new ArachnoidAdaptation(), new DeadlyDerision()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iridescent Blademaster");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A second trigger uses counters gained before the creature leaves")
    void usesLastKnownPowerAfterFirstTriggerAndRemoval() {
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.addToBattlefield(player1, new TributeToTheWorldTree());
        harness.setLibrary(player1, List.of(new IridescentBlademaster()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new IridescentBlademaster());
        harness.setHand(player1, List.of(new DeadlyDerision()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iridescent Blademaster");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing Tribute and the creature does not change the creature's last known power")
    void preservesCreatureInformationWhenTributeAlsoLeaves() {
        Permanent tribute = harness.addToBattlefieldAndReturn(player1, new TributeToTheWorldTree());
        harness.setLibrary(player1, List.of(new IridescentBlademaster()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Naturalize(), new DeadlyDerision()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, tribute.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tribute to the World Tree");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
