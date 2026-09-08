package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SamiteElderTest extends BaseCardTest {

    @Test
    @DisplayName("Grants your creatures protection from the target permanent's colors")
    void grantsProtectionFromTargetPermanentColors() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elder), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opponentBears, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Can target only a permanent you control")
    void cannotTargetOpponentPermanent() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(elder),
                null,
                opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Protection expires at end of turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elder), null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.GREEN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.GREEN)).isFalse();
    }
}
