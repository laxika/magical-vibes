package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fertilid;
import com.github.laxika.magicalvibes.cards.a.AetherHub;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.c.ContaminatedDrink;
import com.github.laxika.magicalvibes.cards.p.PolukranosUnchained;
import com.github.laxika.magicalvibes.cards.a.AsForetold;
import com.github.laxika.magicalvibes.cards.d.DaxosTheReturned;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.n.NissaStewardOfElements;
import com.github.laxika.magicalvibes.cards.w.WhiteManaBattery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Solemnity.class, Fertilid.class, WhiteManaBattery.class, AetherHub.class,
        AsForetold.class, DaxosTheReturned.class, IchorRats.class, NissaStewardOfElements.class,
        Abrade.class, ContaminatedDrink.class, PolukranosUnchained.class})
class SolemnityTest extends BaseCardTest {

    @Test
    @DisplayName("A 0/0 that would enter with +1/+1 counters dies under Solemnity — the counters are never placed")
    @CardUsed({Solemnity.class, Fertilid.class})
    void creatureEntersWithoutCounters() {
        harness.addToBattlefield(player1, new Solemnity());
        harness.setHand(player1, List.of(new Fertilid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Fertilid is printed 0/0 and normally enters with two +1/+1 counters. Solemnity prevents
        // those counters, so it enters as a 0/0 and dies to state-based actions.
        harness.assertNotOnBattlefield(player1, "Fertilid");
        harness.assertInGraveyard(player1, "Fertilid");
    }

    @Test
    @DisplayName("A charge counter can't be put on an artifact while Solemnity is on the battlefield")
    @CardUsed({WhiteManaBattery.class, Solemnity.class})
    void chargeCounterNotPlaced() {
        Permanent battery = addCreatureReady(player1, new WhiteManaBattery());
        harness.addToBattlefield(player1, new Solemnity());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // The cost is still paid (tapped) but the counter is never placed.
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without Solemnity the same activation places the charge counter")
    @CardUsed(WhiteManaBattery.class)
    void chargeCounterPlacedWithoutSolemnity() {
        Permanent battery = addCreatureReady(player1, new WhiteManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @CardUsed({Solemnity.class, AetherHub.class})
    void preventsEnergyCountersEvenWhenOpponentControlsSolemnity() {
        harness.addToBattlefield(player2, new Solemnity());
        gd.setPlayerEnergyCounters(player1.getId(), 2);
        harness.setHand(player1, List.of(new AetherHub()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @CardUsed({Solemnity.class, AetherHub.class})
    void existingEnergyCanStillBeSpent() {
        Permanent hub = addCreatureReady(player1, new AetherHub());
        harness.addToBattlefield(player2, new Solemnity());
        gd.setPlayerEnergyCounters(player1.getId(), 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(hub.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @CardUsed({Solemnity.class, IchorRats.class})
    void preventsPoisonCountersForBothPlayersWithoutRemovingExistingCounters() {
        harness.addToBattlefield(player2, new Solemnity());
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @CardUsed({Solemnity.class, DaxosTheReturned.class})
    void preventsExperienceCountersFromCastingAnotherEnchantment() {
        harness.addToBattlefield(player2, new Solemnity());
        harness.addToBattlefield(player1, new DaxosTheReturned());
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new Solemnity()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @CardUsed({Solemnity.class, AsForetold.class})
    void preventsTimeCountersOnEnchantmentsWithoutRemovingExistingCounters() {
        Permanent foretold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        foretold.setCounterCount(CounterType.TIME, 2);
        harness.addToBattlefield(player2, new Solemnity());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(foretold.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @CardUsed({Solemnity.class, NissaStewardOfElements.class})
    void planeswalkerStillEntersWithLoyaltyAndCanPayPositiveLoyaltyCost() {
        harness.addToBattlefield(player2, new Solemnity());
        harness.setHand(player1, List.of(new NissaStewardOfElements()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castPlaneswalker(player1, 0, 3);
        harness.passBothPriorities();

        Permanent nissa = findPermanent(player1, "Nissa, Steward of Elements");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }
    @Test
    @CardUsed({Solemnity.class, ContaminatedDrink.class})
    void preventsRadiationCountersWithoutPreventingCardDraw() {
        harness.addToBattlefield(player2, new Solemnity());
        gd.playerRadCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new ContaminatedDrink()));
        harness.setLibrary(player1, List.of(new Solemnity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @CardUsed({Solemnity.class, PolukranosUnchained.class, Abrade.class})
    void damagePreventionStillRemovesExistingCounters() {
        Permanent polukranos = harness.addToBattlefieldAndReturn(player2, new PolukranosUnchained());
        polukranos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        harness.addToBattlefield(player1, new Solemnity());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, polukranos.getId());
        harness.passBothPriorities();

        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(polukranos.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Polukranos, Unchained");
    }
}
