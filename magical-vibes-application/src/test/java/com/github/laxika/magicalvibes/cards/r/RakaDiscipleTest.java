package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.b.BattlefieldForge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakaDisciple.class, AngelfireCrusader.class, RazorfinHunter.class, BattlefieldForge.class})
class RakaDiscipleTest extends BaseCardTest {

    @Test
    void preventsNextDamageToCreature() {
        Permanent disciple = addCreatureReady(player1, new RakaDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    void preventsNextDamageToPlayer() {
        addCreatureReady(player1, new RakaDisciple());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void givesTargetCreatureFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new RakaDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void preventsOnlyTheNextDamageToCreature() {
        addCreatureReady(player1, new RakaDisciple());
        Permanent target = addCreatureReady(player2, new AngelfireCrusader());
        addCreatureReady(player1, new RazorfinHunter());
        addCreatureReady(player1, new RazorfinHunter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    void preventsOnlyTheNextDamageToPlayer() {
        addCreatureReady(player1, new RakaDisciple());
        addCreatureReady(player1, new RazorfinHunter());
        addCreatureReady(player1, new RazorfinHunter());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void preventionAbilityCannotTargetLand() {
        addCreatureReady(player1, new RakaDisciple());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BattlefieldForge());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventionShieldExpiresAtEndOfTurn() {
        addCreatureReady(player1, new RakaDisciple());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void flyingAbilityCannotTargetPlayer() {
        addCreatureReady(player1, new RakaDisciple());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
