package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodCultist.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, ResoundingThunder.class})
class BloodCultistTest extends BaseCardTest {

    @Test
    @DisplayName("Ping kills a 1-toughness creature and gains a +1/+1 counter when it dies")
    void pingKillsTargetAndGainsCounter() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player2, new FugitiveWizard());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No counter when the damaged creature survives")
    void noCounterWhenTargetSurvives() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        // The target survives, so the death ability does not trigger.
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new BloodCultist());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void gainsCounterWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noCounterForCreatureNotDamagedByCultist() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canDamageOwnCreatureAndGainCounter() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player1, new FugitiveWizard());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removedSourceDoesNotPutCounterOnAnotherCultist() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent otherCultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player1, 0, cultist.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cultist);
        assertThat(otherCultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tapCostPreventsSecondActivation() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(cultist.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void summoningSicknessPreventsActivation() {
        Permanent cultist = harness.addToBattlefieldAndReturn(player1, new BloodCultist());
        cultist.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(cultist.isTapped()).isFalse();
    }

    @Test
    void gainsCounterFromCombatDamage() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        cultist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, Map.of(0, 0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cultist);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
    @Test
    void damageFromPreviousTurnDoesNotTriggerCounter() {
        Permanent cultist = addCreatureReady(player1, new BloodCultist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.getMarkedDamage()).isZero();
        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(cultist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
