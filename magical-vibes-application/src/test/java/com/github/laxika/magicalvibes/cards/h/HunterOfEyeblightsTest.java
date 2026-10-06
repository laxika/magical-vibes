package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HunterOfEyeblights.class, WoodlandChangeling.class})
class HunterOfEyeblightsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you don't control")
    void etbPutsCounterOnOpponentCreature() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");

        harness.setHand(player1, List.of(new HunterOfEyeblights()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent target = findPermanent(player2, "Woodland Changeling");
        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target a creature you control")
    void etbCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new WoodlandChangeling());
        UUID targetId = harness.getPermanentId(player1, "Woodland Changeling");

        harness.setHand(player1, List.of(new HunterOfEyeblights()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability destroys target creature that has a counter")
    void abilityDestroysCreatureWithCounter() {
        Permanent hunter = addReadyHunter(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(hunter.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Ability cannot target a creature without a counter")
    void abilityCannotTargetCreatureWithoutCounter() {
        addReadyHunter(player1);
        Permanent target = addCreatureReady(player2, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can destroy a creature you control")
    void abilityDestroysOwnCreatureWithCounter() {
        addReadyHunter(player1);
        Permanent target = addCreatureWithCounter(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Ability accepts counters other than +1/+1 counters")
    void abilityDestroysCreatureWithMinusOneCounter() {
        addReadyHunter(player1);
        Permanent target = addCreatureReady(player2, new WoodlandChangeling());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Ability does not destroy a target whose last counter is removed before resolution")
    void abilityFizzlesWhenLastCounterIsRemoved() {
        addReadyHunter(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Woodland Changeling");
        harness.assertNotInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Ability still destroys a target if another counter type remains")
    void abilityResolvesWhenAnotherCounterRemains() {
        addReadyHunter(player1);
        Permanent target = addCreatureWithCounter(player2);
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Hunter can target itself when it has a counter")
    void abilityCanDestroyItself() {
        Permanent hunter = addReadyHunter(player1);
        hunter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, hunter.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hunter of Eyeblights");
        harness.assertInGraveyard(player1, "Hunter of Eyeblights");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSickHunterCannotActivate() {
        harness.addToBattlefield(player1, new HunterOfEyeblights());
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Hunter cannot activate")
    void tappedHunterCannotActivate() {
        Permanent hunter = addReadyHunter(player1);
        hunter.tap();
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation requires black mana")
    void abilityCannotBePaidWithOnlyColorlessMana() {
        addReadyHunter(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hunter enters even when no opposing creature can be targeted")
    void entersWithoutLegalEtbTarget() {
        harness.castFromHand(player1, new HunterOfEyeblights(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hunter of Eyeblights");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not add a counter if its target comes under your control")
    void etbTargetMustRemainOutsideYourControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HunterOfEyeblights()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getPlusOnePlusOneCounters()).isZero();
        harness.assertOnBattlefield(player1, "Woodland Changeling");
    }

    private Permanent addReadyHunter(Player player) {
        return addCreatureReady(player, new HunterOfEyeblights());
    }

    private Permanent addCreatureWithCounter(Player player) {
        Permanent perm = addCreatureReady(player, new WoodlandChangeling());
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return perm;
    }
}
