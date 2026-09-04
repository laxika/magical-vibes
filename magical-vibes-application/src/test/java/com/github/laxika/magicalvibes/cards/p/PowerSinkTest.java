package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BadRiver;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.d.DwarvenMiner;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerSink.class, DwarvenMiner.class, BadRiver.class, BirdsOfParadise.class, CrystalVein.class,
        Forest.class, ManaPrism.class})
class PowerSinkTest extends BaseCardTest {

    private DwarvenMiner prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        DwarvenMiner miner = new DwarvenMiner();
        harness.setHand(player1, List.of(miner));
        return miner;
    }

    @Test
    @DisplayName("Counters and taps lands + empties mana when the controller cannot pay X")
    void countersAndPunishesWhenCannotPay() {
        DwarvenMiner miner = prepareCounterTarget();
        harness.addToBattlefield(player1, new CrystalVein());
        harness.addToBattlefield(player1, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // {1}{R} to cast Miner, 1 left over (< X)

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {U} + X=3

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, miner.getId()); // X = 3
        harness.passBothPriorities();

        // Spell countered (player1 could not pay {3}).
        harness.assertInGraveyard(player1, "Dwarven Miner");
        harness.assertNotOnBattlefield(player1, "Dwarven Miner");
        // Rider: all of player1's lands are tapped and their mana pool is emptied.
        assertThat(p1Battlefield).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Does not tap lands without mana abilities")
    void doesNotTapLandsWithoutManaAbilities() {
        DwarvenMiner miner = prepareCounterTarget();
        Permanent nonManaLand = harness.addToBattlefieldAndReturn(player1, new BadRiver());
        Permanent manaLand = harness.addToBattlefieldAndReturn(player1, new CrystalVein());
        Permanent manaArtifact = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        Permanent opponentManaLand = harness.addToBattlefieldAndReturn(player2, new CrystalVein());
        nonManaLand.enterUntapped();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, miner.getId());
        harness.passBothPriorities();

        assertThat(nonManaLand.isTapped()).isFalse();
        assertThat(manaLand.isTapped()).isTrue();
        assertThat(manaArtifact.isTapped()).isFalse();
        assertThat(opponentManaLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap non-land permanents with mana abilities")
    void doesNotTapNonLandManaSources() {
        DwarvenMiner miner = prepareCounterTarget();
        Permanent manaCreature = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
        Permanent manaLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, miner.getId());
        harness.passBothPriorities();

        assertThat(manaCreature.isTapped()).isFalse();
        assertThat(manaLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters and taps lands + empties mana when the controller declines to pay X")
    void countersAndPunishesWhenDeclines() {
        DwarvenMiner miner = prepareCounterTarget();
        harness.addToBattlefield(player1, new CrystalVein());
        harness.addToBattlefield(player1, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // {1}{R} to cast Miner, 1 left over (can pay X=1)

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, miner.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false); // decline to pay

        harness.assertInGraveyard(player1, "Dwarven Miner");
        assertThat(p1Battlefield).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Paying X keeps the spell and leaves lands untapped")
    void payingKeepsSpellAndSparesLands() {
        DwarvenMiner miner = prepareCounterTarget();
        harness.addToBattlefield(player1, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // {1}{R} to cast Miner, 1 to pay X=1

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, miner.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // pay {1}

        // Not countered and the rider did not fire.
        harness.assertNotInGraveyard(player1, "Dwarven Miner");
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities(); // resolve Dwarven Miner
        harness.assertOnBattlefield(player1, "Dwarven Miner");
    }

    @Test
    @DisplayName("X=0 can still be declined, causing the counter and not-paid rider")
    void decliningZeroPaymentCountersAndPunishes() {
        DwarvenMiner miner = prepareCounterTarget();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 1); // {U} + X=0

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, miner.getId()); // X = 0
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Dwarven Miner");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
