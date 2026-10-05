package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KytheonsIrregulars.class, YokedOx.class, Forest.class})
class KytheonsIrregularsTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent irregulars = addCreatureReady(player1, new KytheonsIrregulars());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(irregulars.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(irregulars.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent irregulars = addCreatureReady(player1, new KytheonsIrregulars());
        irregulars.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(irregulars.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Renown does not trigger when the creature is blocked")
    void noRenownWhenBlocked() {
        Permanent irregulars = addCreatureReady(player1, new KytheonsIrregulars());
        addCreatureReady(player2, new YokedOx());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(irregulars.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("{W}{W} taps target creature")
    void abilityTapsTargetCreature() {
        addCreatureReady(player1, new KytheonsIrregulars());
        Permanent creature = addCreatureReady(player2, new YokedOx());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new KytheonsIrregulars());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The ability can be activated while summoning sick and tapped")
    void abilityWorksWhileSummoningSickAndTapped() {
        Permanent irregulars = harness.addToBattlefieldAndReturn(player1, new KytheonsIrregulars());
        irregulars.setSummoningSick(true);
        irregulars.setTapped(true);
        Permanent creature = addCreatureReady(player2, new YokedOx());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(irregulars.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can be activated repeatedly without tapping its source")
    void abilityCanBeActivatedRepeatedly() {
        Permanent irregulars = addCreatureReady(player1, new KytheonsIrregulars());
        Permanent first = addCreatureReady(player2, new YokedOx());
        Permanent second = addCreatureReady(player2, new YokedOx());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.activateAbility(player1, 0, null, second.getId());
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(irregulars.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can target itself even when already tapped")
    void abilityCanTargetAlreadyTappedSelf() {
        Permanent irregulars = addCreatureReady(player1, new KytheonsIrregulars());
        irregulars.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, irregulars.getId());
        harness.passBothPriorities();

        assertThat(irregulars.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability requires two white mana")
    void abilityCannotBePaidWithOneWhiteAndOneColorless() {
        addCreatureReady(player1, new KytheonsIrregulars());
        Permanent creature = addCreatureReady(player2, new YokedOx());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
