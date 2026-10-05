package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.FortifyingProvisions;
import com.github.laxika.magicalvibes.cards.q.QuestingBeast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@CardUsed({OathswornKnight.class, Shock.class, FortifyingProvisions.class, QuestingBeast.class})
class OathswornKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Oathsworn Knight enters with four +1/+1 counters")
    void entersWithFourCounters() {
        harness.setHand(player1, List.of(new OathswornKnight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Oathsworn Knight");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Damage is prevented and removes one counter per damage event")
    void damageRemovesOneCounterPerEvent() {
        Permanent knight = addCreatureReady(player2, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oathsworn Knight");
    }

    @Test
    @DisplayName("Oathsworn Knight must attack each combat when able")
    void mustAttackWhenAble() {
        Permanent knight = addCreatureReady(player1, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void damageIsNotPreventedWithoutCounters() {
        harness.addToBattlefield(player2, new FortifyingProvisions());
        Permanent knight = addCreatureReady(player2, new OathswornKnight());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Oathsworn Knight");
        harness.assertNotOnBattlefield(player2, "Oathsworn Knight");
    }

    @Test
    void lastCounterPreventsTheWholeDamageEvent() {
        harness.addToBattlefield(player2, new FortifyingProvisions());
        Permanent knight = addCreatureReady(player2, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oathsworn Knight");
    }

    @Test
    void losingLastCounterWithoutToughnessBoostPutsKnightInGraveyard() {
        Permanent knight = addCreatureReady(player2, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Oathsworn Knight");
    }

    @Test
    void separateDamageEventsEachRemoveOneCounter() {
        Permanent knight = addCreatureReady(player2, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oathsworn Knight");
    }

    @Test
    void simultaneousDamageFromMultipleBlockersRemovesOnlyOneCounter() {
        Permanent attacker = addCreatureReady(player1, new OathswornKnight());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        attacker.setAttacking(true);
        List<Permanent> blockers = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Permanent blocker = addCreatureReady(player2, new OathswornKnight());
            blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            blockers.add(blocker);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blockers.get(0).getId(), 4));

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Oathsworn Knight");
    }

    @Test
    void unpreventableCombatDamageStillRemovesOneCounter() {
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        Permanent knight = addCreatureReady(player2, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        knight.setBlocking(true);
        knight.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player2, "Oathsworn Knight");
    }

    @Test
    void tappedKnightDoesNotHaveToAttack() {
        Permanent knight = addCreatureReady(player1, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        knight.setTapped(true);

        assertDoesNotThrow(() -> declareAttackers(player1, List.of()));
    }

    @Test
    void summoningSickKnightDoesNotHaveToAttack() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertDoesNotThrow(() -> declareAttackers(player1, List.of()));
    }
}
