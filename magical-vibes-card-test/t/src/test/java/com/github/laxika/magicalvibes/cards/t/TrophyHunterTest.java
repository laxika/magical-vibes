package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurningPalmEfreet;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.d.DrakeFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrophyHunter.class, BurningPalmEfreet.class, CourierHawk.class, DrakeFamiliar.class,
        GrayscaledGharial.class, ViashinoFangtail.class})
class TrophyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a flying creature and gets a +1/+1 counter when it dies")
    void damagesFlyingCreatureAndGainsCounterWhenItDies() {
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        Permanent target = addCreatureReady(player2, new DrakeFamiliar());

        activateHunter(target);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Does not get a counter when the damaged flying creature survives")
    void noCounterWhenTargetSurvives() {
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        Permanent target = addCreatureReady(player2, new CourierHawk());

        activateHunter(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isOne();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets a counter when another source deals the lethal damage")
    void gainsCounterWhenAnotherSourceDealsLethalDamage() {
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        addCreatureReady(player1, new ViashinoFangtail());
        Permanent target = addCreatureReady(player2, new CourierHawk());

        activateHunter(target);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Checks flying when the damaged creature dies")
    void noCounterWhenTargetLosesFlyingBeforeItDies() {
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        addCreatureReady(player1, new BurningPalmEfreet());
        Permanent target = addCreatureReady(player2, new CourierHawk());

        activateHunter(target);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        addCreatureReady(player1, new TrophyHunter());
        Permanent target = addCreatureReady(player2, new GrayscaledGharial());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }

    @Test
    @DisplayName("Repeated damage to one creature gives only one counter when it dies")
    void repeatedDamageGivesOneCounter() {
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        Permanent target = addCreatureReady(player2, new CourierHawk());

        activateHunter(target);
        harness.passBothPriorities();
        activateHunter(target);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("A flying creature killed without damage from Trophy Hunter gives no counter")
    void undamagedCreatureDeathGivesNoCounter() {
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        addCreatureReady(player1, new ViashinoFangtail());
        Permanent target = addCreatureReady(player2, new DrakeFamiliar());

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A tapped Trophy Hunter can damage its controller's flying creature")
    void tappedHunterCanDamageOwnCreature() {
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        hunter.tap();
        Permanent target = addCreatureReady(player1, new DrakeFamiliar());

        activateHunter(target);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(hunter.isTapped()).isTrue();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    private void activateHunter(Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
    }

}
