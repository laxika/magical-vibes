package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KurbisHarvestCelebrant.class, GrizzlyBears.class, Shock.class})
class KurbisHarvestCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters equal to the mana spent to cast it")
    void entersWithCountersEqualToManaSpent() {
        harness.setHand(player1, List.of(new KurbisHarvestCelebrant()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent kurbis = findPermanent(player1, "Kurbis, Harvest Celebrant");
        assertThat(kurbis).isNotNull();
        assertThat(kurbis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removes a counter to prevent all damage to another countered creature")
    void preventsDamageToAnotherCounteredCreature() {
        Permanent kurbis = harness.addToBattlefieldAndReturn(player1, new KurbisHarvestCelebrant());
        kurbis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(kurbis), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(kurbis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target itself or a creature without a +1/+1 counter")
    void requiresAnotherCounteredCreature() {
        Permanent kurbis = harness.addToBattlefieldAndReturn(player1, new KurbisHarvestCelebrant());
        kurbis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kurbis), 0, null, kurbis.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kurbis), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kurbis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with X zero still gives two counters from the green mana spent")
    void entersWithTwoCountersWhenXIsZero() {
        harness.setHand(player1, List.of(new KurbisHarvestCelebrant()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kurbis = findPermanent(player1, "Kurbis, Harvest Celebrant");
        assertThat(kurbis).isNotNull();
        assertThat(kurbis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the last counter does not stop the activated ability from resolving")
    void lastCounterCanProtectAnOpponentsCreature() {
        Permanent kurbis = harness.addToBattlefieldAndReturn(player1, new KurbisHarvestCelebrant());
        kurbis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(kurbis), 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kurbis, Harvest Celebrant");
        harness.assertNotOnBattlefield(player1, "Kurbis, Harvest Celebrant");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature losing its counter before resolution is no longer a legal target")
    void targetLosingCounterBeforeResolutionIsNotProtected() {
        Permanent kurbis = harness.addToBattlefieldAndReturn(player1, new KurbisHarvestCelebrant());
        kurbis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(kurbis), 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(kurbis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Protection persists after the target loses its counter and prevents repeated damage")
    void protectionPersistsWithoutCountersForMultipleDamageEvents() {
        Permanent kurbis = harness.addToBattlefieldAndReturn(player1, new KurbisHarvestCelebrant());
        kurbis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(kurbis), 0, null, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage prevention expires at the end of the turn")
    void protectionExpiresOnTheNextTurn() {
        Permanent kurbis = harness.addToBattlefieldAndReturn(player1, new KurbisHarvestCelebrant());
        kurbis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(kurbis), 0, null, target.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
