package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RageForger.class, ElvishWarrior.class})
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

        harness.passBothPriorities(); // resolve the attack trigger — presents the may choice
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());

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
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Attacking creature with a +1/+1 counter may ping a planeswalker")
    void attackingWithCounterMayPingPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        addCreatureReady(player1, new RageForger());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }
}
