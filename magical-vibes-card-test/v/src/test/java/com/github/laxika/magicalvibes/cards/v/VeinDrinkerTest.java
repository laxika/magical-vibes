package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.g.GuardiansOfAkrasa;
import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeinDrinker.class, CylianElf.class, GuardiansOfAkrasa.class, Forest.class,
        ResoundingWave.class, ResoundingThunder.class})
class VeinDrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals reciprocal power damage and gains a counter when the target dies")
    void damageKillsTargetAndGainsCounter() {
        Permanent drinker = addCreatureReady(player1, new VeinDrinker());
        Permanent target = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        // Target destroyed by lethal power damage
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        // Vein Drinker took 2 reciprocal damage but survives
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(drinker);
        assertThat(drinker.getMarkedDamage()).isEqualTo(2);
        // +1/+1 counter from the damaged creature dying
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No counter when the damaged creature survives")
    void noCounterWhenTargetSurvives() {
        Permanent drinker = addCreatureReady(player1, new VeinDrinker());
        Permanent target = addCreatureReady(player2, new GuardiansOfAkrasa());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        // Target survives 4 damage on 5 toughness — no death, no counter
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new VeinDrinker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Deals damage using last known power after leaving the battlefield")
    void dealsDamageAfterSourceLeaves() {
        Permanent drinker = addCreatureReady(player1, new VeinDrinker());
        drinker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GuardiansOfAkrasa());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, drinker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(drinker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GuardiansOfAkrasa);
    }

    @Test
    @DisplayName("Gets a counter when a previously damaged creature dies later that turn")
    void gainsCounterForLaterDeath() {
        Permanent drinker = addCreatureReady(player1, new VeinDrinker());
        Permanent target = addCreatureReady(player2, new GuardiansOfAkrasa());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can damage and gain a counter from a creature its controller owns")
    void canTargetOwnCreature() {
        Permanent drinker = addCreatureReady(player1, new VeinDrinker());
        Permanent target = addCreatureReady(player1, new CylianElf());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).contains(drinker);
        assertThat(drinker.getMarkedDamage()).isEqualTo(2);
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No damage is dealt when the target leaves before resolution")
    void noDamageWhenTargetLeaves() {
        Permanent drinker = addCreatureReady(player1, new VeinDrinker());
        Permanent target = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(drinker.getMarkedDamage()).isZero();
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card instanceof CylianElf);
    }
    @Test
    @DisplayName("Both creatures die before a counter can save Vein Drinker")
    void simultaneousLethalDamage() {
        Permanent drinker = addCreatureReady(player1, new VeinDrinker());
        Permanent target = addCreatureReady(player2, new VeinDrinker());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(drinker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

}
