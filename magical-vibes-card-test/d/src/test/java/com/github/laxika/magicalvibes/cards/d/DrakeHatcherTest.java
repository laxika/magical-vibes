package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.Omniscience;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrakeHatcher.class, Omniscience.class})
class DrakeHatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Puts incubation counters on itself equal to combat damage dealt to a player")
    void combatDamageAddsIncubationCounters() {
        Permanent hatcher = addReadyHatcher();
        hatcher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hatcher.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isZero();

        harness.passBothPriorities();

        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing three incubation counters creates a 2/2 blue Drake with flying")
    void removesCountersAndCreatesDrake() {
        Permanent hatcher = addReadyHatcher();
        hatcher.setCounterCount(CounterType.INCUBATION, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getColor() == CardColor.BLUE
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 2
                        && permanent.getCard().getSubtypes().contains(CardSubtype.DRAKE)
                        && permanent.getCard().getKeywords().contains(Keyword.FLYING));
    }

    @Test
    @DisplayName("Cannot activate without three incubation counters")
    void cannotActivateWithoutEnoughCounters() {
        addReadyHatcher();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHatcher() {
        return addCreatureReady(player1, new DrakeHatcher());
    }

    @Test
    void countersArePaidImmediatelyAndCanFundMultipleActivations() {
        Permanent hatcher = addReadyHatcher();
        hatcher.setCounterCount(CounterType.INCUBATION, 7);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(4);
        assertThat(countPermanents(player1, "Drake")).isZero();

        harness.activateAbility(player1, 0, null, null);

        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(2);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hatcher = harness.addToBattlefieldAndReturn(player1, new DrakeHatcher());
        hatcher.setSummoningSick(true);
        hatcher.tap();
        hatcher.setCounterCount(CounterType.INCUBATION, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
        assertThat(hatcher.isTapped()).isTrue();
    }

    @Test
    void twoIncubationCountersAndOtherCounterTypesCannotPayCost() {
        Permanent hatcher = addReadyHatcher();
        hatcher.setCounterCount(CounterType.INCUBATION, 2);
        hatcher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(2);
        assertThat(hatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void vigilanceKeepsAttackerUntapped() {
        Permanent hatcher = addReadyHatcher();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(hatcher.isAttacking()).isTrue();
        assertThat(hatcher.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();
        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(1);
    }

    @Test
    void prowessIncreasesCombatDamageAndIncubationCounters() {
        Permanent hatcher = addReadyHatcher();
        harness.castFromHand(player1, new Omniscience(), "{7}{U}{U}{U}");
        resolveAllTriggers();
        hatcher.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(2);
    }

    @Test
    void castingCreatureDoesNotTriggerProwess() {
        Permanent hatcher = addReadyHatcher();
        harness.castFromHand(player1, new DrakeHatcher(), "{1}{U}");
        resolveAllTriggers();
        hatcher.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(1);
    }

    @Test
    void combatDamageToCreatureDoesNotAddIncubationCounters() {
        Permanent attacker = addReadyHatcher();
        Permanent blocker = addCreatureReady(player2, new DrakeHatcher());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.INCUBATION)).isZero();
        assertThat(blocker.getCounterCount(CounterType.INCUBATION)).isZero();
    }
}
