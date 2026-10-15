package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoForBlood;
import com.github.laxika.magicalvibes.cards.h.HumbleNaturalist;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
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

@CardUsed({StormwildCapridor.class, GrizzlyBears.class, LeylineOfPunishment.class, Shock.class,
        GoForBlood.class, HumbleNaturalist.class})
class StormwildCapridorTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage to itself and gets counters for damage prevented")
    void preventsNoncombatDamageAndAddsCounters() {
        Permanent capridor = harness.addToBattlefieldAndReturn(player2, new StormwildCapridor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, capridor.getId());

        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(capridor.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage to another creature")
    void doesNotProtectAnotherCreature() {
        Permanent capridor = harness.addToBattlefieldAndReturn(player2, new StormwildCapridor());
        GrizzlyBears otherCard = new GrizzlyBears();
        otherCard.setToughness(3);
        Permanent other = harness.addToBattlefieldAndReturn(player2, otherCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not prevent combat damage")
    void doesNotPreventCombatDamage() {
        Permanent capridor = harness.addToBattlefieldAndReturn(player2, new StormwildCapridor());
        capridor.setSummoningSick(false);
        capridor.setBlocking(true);
        capridor.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(capridor.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not add counters when damage cannot be prevented")
    void doesNotAddCountersForUnpreventableDamage() {
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        Permanent capridor = harness.addToBattlefieldAndReturn(player2, new StormwildCapridor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, capridor.getId());

        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(capridor.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fight damage is prevented without increasing the damage dealt in that fight")
    void preventsFightDamageAsFirstCreature() {
        Permanent capridor = harness.addToBattlefieldAndReturn(player1, new StormwildCapridor());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HumbleNaturalist());
        harness.setHand(player1, List.of(new GoForBlood()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(capridor.getId(), opponent.getId()));

        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(capridor.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Stormwild Capridor");
        harness.assertOnBattlefield(player2, "Humble Naturalist");
    }

    @Test
    @DisplayName("Counters from preventing fight damage do not increase outgoing damage as the second creature")
    void preventsFightDamageAsSecondCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HumbleNaturalist());
        Permanent capridor = harness.addToBattlefieldAndReturn(player2, new StormwildCapridor());
        harness.setHand(player1, List.of(new GoForBlood()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), capridor.getId()));

        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(capridor.getMarkedDamage()).isZero();
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Humble Naturalist");
        harness.assertOnBattlefield(player2, "Stormwild Capridor");
    }

    @Test
    @DisplayName("Prevents spell damage during combat and accumulates counters from separate events")
    void preventsOwnSpellDamageDuringCombat() {
        Permanent capridor = harness.addToBattlefieldAndReturn(player1, new StormwildCapridor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, capridor.getId());
        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, capridor.getId());

        assertThat(capridor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(capridor.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Stormwild Capridor");
    }
}
