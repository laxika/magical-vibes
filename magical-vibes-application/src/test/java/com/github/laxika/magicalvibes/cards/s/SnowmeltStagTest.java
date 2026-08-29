package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }
}
