package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TwitchingDoll.class)
class TwitchingDollTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds mana and a nest counter")
    void manaAbilityAddsManaAndNestCounter() {
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(doll.getCounterCount(CounterType.NEST)).isEqualTo(1);
        assertThat(doll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability creates one Spider token per counter")
    void sacrificeAbilityCreatesSpidersForAllCounters() {
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);
        doll.setCounterCount(CounterType.NEST, 2);
        doll.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Twitching Doll");
        harness.assertInGraveyard(player1, "Twitching Doll");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated as a sorcery")
    void sacrificeAbilityIsSorcerySpeed() {
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);
        doll.setCounterCount(CounterType.NEST, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
