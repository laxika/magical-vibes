package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElectricSeaweed.class, BalduvianBears.class, FyndhornElves.class, Shock.class, WallOfWood.class})
class ElectricSeaweedTest extends BaseCardTest {

    @Test
    @DisplayName("Its temporary death trigger damages non-Wall creatures but not Walls")
    void deathTriggerDamagesNonWallCreatures() {
        Permanent nonWall = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());
        Permanent seaweed = castElectricSeaweed(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dyingCreature.getId());
        harness.passBothPriorities();

        assertThat(nonWall.getMarkedDamage()).isEqualTo(1);
        assertThat(wall.getMarkedDamage()).isZero();
        assertThat(seaweed.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to any target")
    void tapAbilityDealsDamageToTargetPlayer() {
        Permanent seaweed = addReadySeaweed(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(seaweed.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The tap ability can target Walls even though the death trigger cannot damage them")
    void tapAbilityDamagesWall() {
        addReadySeaweed(player1);
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Haste allows the tap ability on the turn Seaweed enters")
    void tapAbilityCanKillCreatureImmediatelyAndStartDeathChain() {
        Permanent firstElf = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());
        harness.addToBattlefield(player1, new FyndhornElves());
        harness.addToBattlefield(player2, new BalduvianBears());
        castElectricSeaweed(player1);

        harness.activateAbility(player1, 1, null, firstElf.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fyndhorn Elves");
        harness.assertInGraveyard(player2, "Fyndhorn Elves");
        harness.assertInGraveyard(player2, "Balduvian Bears");
        harness.assertOnBattlefield(player1, "Electric Seaweed");
    }

    @Test
    @DisplayName("The delayed death trigger continues after Seaweed dies")
    void delayedTriggerSurvivesSourceDeath() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());
        Permanent seaweed = castElectricSeaweed(player1);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, seaweed.getId());
        harness.castAndResolveInstant(player1, 0, seaweed.getId());
        harness.assertInGraveyard(player1, "Electric Seaweed");
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();

        harness.castAndResolveInstant(player1, 0, elf.getId());
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry trigger creates the delayed trigger even if Seaweed has already died")
    void delayedTriggerIsCreatedAfterSourceDiesInResponse() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());
        harness.setHand(player1, List.of(new ElectricSeaweed()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent seaweed = findPermanent(player1, "Electric Seaweed");
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, seaweed.getId());
        harness.castAndResolveInstant(player1, 0, seaweed.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, elf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Electric Seaweed");
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deaths before the entry trigger resolves do not trigger damage")
    void deathBeforeEntryTriggerResolvesDoesNotDealDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());
        harness.setHand(player1, List.of(new ElectricSeaweed()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, elf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fyndhorn Elves");
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The delayed death trigger expires at the end of the turn")
    void deathTriggerExpiresAfterTurnEnds() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());
        castElectricSeaweed(player1);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, elf.getId());

        harness.assertInGraveyard(player2, "Fyndhorn Elves");
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castElectricSeaweed(Player player) {
        harness.setHand(player, List.of(new ElectricSeaweed()));
        harness.addMana(player, ManaColor.RED, 4);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Electric Seaweed");
    }

    private Permanent addReadySeaweed(Player player) {
        Permanent seaweed = harness.addToBattlefieldAndReturn(player, new ElectricSeaweed());
        seaweed.setSummoningSick(false);
        return seaweed;
    }
}
