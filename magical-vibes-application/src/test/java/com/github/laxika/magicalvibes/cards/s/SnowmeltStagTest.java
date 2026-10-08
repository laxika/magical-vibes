package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SnowmeltStag.class)
class SnowmeltStagTest extends BaseCardTest {

    @Test
    @DisplayName("Has base power and toughness 5/2 during its controller's turn")
    void hasAggressiveStatsDuringControllerTurn() {
        Permanent stag = harness.addToBattlefieldAndReturn(player1, new SnowmeltStag());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, stag)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stag)).isEqualTo(2);
    }

    @Test
    @DisplayName("Has printed base power and toughness 2/5 during an opponent's turn")
    void hasDefensiveStatsDuringOpponentsTurn() {
        Permanent stag = harness.addToBattlefieldAndReturn(player1, new SnowmeltStag());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, stag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stag)).isEqualTo(5);
    }

    @Test
    @DisplayName("Resolving the ability makes Snowmelt Stag unblockable this turn")
    void abilityMakesSelfUnblockable() {
        Permanent stag = harness.addToBattlefieldAndReturn(player1, new SnowmeltStag());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(stag.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off during cleanup")
    void unblockableWearsOff() {
        Permanent stag = harness.addToBattlefieldAndReturn(player1, new SnowmeltStag());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(stag.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stag.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Attacking with vigilance does not tap Snowmelt Stag")
    void attackingDoesNotTap() {
        Permanent stag = addCreatureReady(player1, new SnowmeltStag());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(stag.isAttacking()).isTrue();
        assertThat(stag.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counters apply after the base stats change on each player's turn")
    void countersApplyToBothBaseStatConfigurations() {
        Permanent stag = harness.addToBattlefieldAndReturn(player1, new SnowmeltStag());
        stag.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, stag)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, stag)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, stag)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stag)).isEqualTo(6);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, stag)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, stag)).isEqualTo(3);
    }

    @Test
    @DisplayName("The resolved ability prevents blocking only its source")
    void abilityPreventsBlockingOnlyItsSource() {
        Permanent stag = addCreatureReady(player1, new SnowmeltStag());
        Permanent otherStag = addCreatureReady(player1, new SnowmeltStag());
        Permanent blocker = addCreatureReady(player2, new SnowmeltStag());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(stag.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(bls.canBlockAttacker(gd, blocker, stag, List.of(blocker))).isFalse();
        assertThat(bls.canBlockAttacker(gd, blocker, otherStag, List.of(blocker))).isTrue();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void tappedSummoningSickCreatureCanActivateAbility() {
        Permanent stag = harness.addToBattlefieldAndReturn(player1, new SnowmeltStag());
        stag.setSummoningSick(true);
        stag.setTapped(true);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(stag.isCantBeBlocked()).isTrue();
        assertThat(stag.isTapped()).isTrue();
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }
}
