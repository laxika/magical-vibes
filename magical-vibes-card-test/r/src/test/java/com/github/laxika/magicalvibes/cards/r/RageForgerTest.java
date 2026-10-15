package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.FaithsShield;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RageForger.class, ElvishWarrior.class, ChandraNalaar.class, FaithsShield.class})
class RageForgerTest extends BaseCardTest {

    // "When this creature enters, put a +1/+1 counter on each other Shaman creature you control.
    //  Whenever a creature you control with a +1/+1 counter on it attacks, you may have that
    //  creature deal 1 damage to target player or planeswalker."

    @Test
    @DisplayName("ETB puts a +1/+1 counter on each other Shaman, not on itself or non-Shamans")
    void etbCountersOtherShamans() {
        Permanent otherShaman = addCreatureReady(player1, new RageForger()); // Elemental Shaman
        Permanent nonShaman = addCreatureReady(player1, new ElvishWarrior());  // Elf Warrior, not a Shaman
        Permanent opponentShaman = addCreatureReady(player2, new RageForger());

        harness.setHand(player1, List.of(new RageForger()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the spell — it enters, ETB trigger onto stack
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(otherShaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonShaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentShaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent entered = findPermanents(player1, "Rage Forger").stream()
                .filter(p -> p != otherShaman)
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // The ping targets the *controller* (a legal "any player" target) so that combat damage from
    // the attacker — which hits the defending player2 — doesn't confound the assertion. player1 is
    // never combat-damaged, so its life reflects the ping alone.

    @Test
    @DisplayName("Attacking creature with a +1/+1 counter may ping the chosen player")
    void attackingWithCounterMayPingPlayer() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RageForger());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1)); // attack with the counter-bearing Elf Warrior
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities(); // resolve the attack trigger — presents the may choice
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining the may ability deals no ping damage")
    void decliningDealsNoDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RageForger());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities(); // resolve the attack trigger — presents the may choice
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Attacking creature without a +1/+1 counter does not trigger")
    void attackingWithoutCounterDoesNotTrigger() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RageForger());
        addCreatureReady(player1, new ElvishWarrior()); // no counter

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Attacking creature with a +1/+1 counter may ping a planeswalker")
    void attackingWithCounterMayPingPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        addCreatureReady(player1, new RageForger());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, planeswalker.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Rage Forger can trigger on its own attack if it has a +1/+1 counter")
    void ownAttackWithCounterTriggers() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player1, new RageForger());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Removing the attacker's counter after it attacks does not stop the damage")
    void removingCounterAfterAttackDoesNotStopTrigger() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RageForger());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, player1.getId());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Multiple counters still produce only one damage per attacking creature")
    void multipleCountersDoNotIncreaseDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RageForger());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Each attacking creature with a counter produces a separate optional trigger")
    void eachAttackerTriggersSeparately() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RageForger());
        Permanent first = addCreatureReady(player1, new ElvishWarrior());
        Permanent second = addCreatureReady(player1, new ElvishWarrior());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1, 2));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Planeswalker protection checks the attacking creature's color for damage")
    void planeswalkerProtectionUsesAttackerColor() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RageForger());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, planeswalker.getId());
        harness.handleListChoice(player1, "GREEN");

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }
}
