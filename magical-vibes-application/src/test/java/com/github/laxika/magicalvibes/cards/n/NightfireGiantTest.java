package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightfireGiant.class, Forest.class, Mountain.class, ElvishMystic.class, AjaniSteadfast.class})
class NightfireGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/3 without a Mountain")
    void noBoostWithoutMountain() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new NightfireGiant());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 while you control a Mountain")
    void boostWithMountain() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new NightfireGiant());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's Mountain does not grant the boost")
    void opponentMountainDoesNotCount() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new NightfireGiant());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target player")
    void abilityDamagesPlayer() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void abilityDamagesCreature() {
        harness.addToBattlefield(player1, new NightfireGiant());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());
        harness.addMana(player1, ManaColor.RED, 5);

        UUID elvesId = elves.getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elvish Mystic");
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleMountainsDoNotMultiplyBoost() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new NightfireGiant());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    void boostUpdatesWhenMountainEntersAndLeaves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new NightfireGiant());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(mountain);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void abilityRequiresRedMana() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedSummoningSickGiantCanActivateRepeatedly() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new NightfireGiant());
        giant.setSummoningSick(true);
        giant.tap();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    void abilityCanTargetItsController() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void abilityDamagesPlaneswalker() {
        harness.addToBattlefield(player1, new NightfireGiant());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniSteadfast());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, ajani.getId());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }
}
