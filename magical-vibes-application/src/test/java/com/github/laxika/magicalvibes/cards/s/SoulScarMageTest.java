package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.a.AncientCrab;
import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulScarMage.class, HillGiant.class, Shock.class, AncientCrab.class, Combust.class, TurnToFrog.class})
class SoulScarMageTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to an opponent's creature is dealt as -1/-1 counters instead")
    void noncombatDamageToOpponentCreatureBecomesCounters() {
        harness.addToBattlefield(player1, new SoulScarMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        UUID targetId = giant.getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve Shock

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(giant.getMarkedDamage()).isZero();
        // 3/3 with two -1/-1 counters survives as a 1/1.
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Noncombat damage to your own creature is dealt normally (opponent-only)")
    void noncombatDamageToOwnCreatureIsNormal() {
        harness.addToBattlefield(player1, new SoulScarMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        UUID targetId = giant.getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve Shock

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void prowessPumpsOnNoncreatureSpell() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SoulScarMage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
    }

    @Test
    void unpreventableDamageIsStillReplacedWithCounters() {
        harness.addToBattlefield(player1, new SoulScarMage());
        Permanent crab = harness.addToBattlefieldAndReturn(player2, new AncientCrab());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, crab.getId());
        harness.passBothPriorities();

        assertThat(crab.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(crab.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Ancient Crab");
    }

    @Test
    void losingAbilitiesDisablesDamageReplacement() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SoulScarMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, mage.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SoulScarMage());
        harness.setHand(player1, List.of(new AncientCrab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Ancient Crab");
    }

    @Test
    void opposingSourceDoesNotUseYourReplacementOrTriggerProwess() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SoulScarMage());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, giant.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(2);
    }

    @Test
    void prowessBonusExpiresAtEndOfTurn() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SoulScarMage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
        harness.assertLife(player2, 18);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(2);
    }
}
