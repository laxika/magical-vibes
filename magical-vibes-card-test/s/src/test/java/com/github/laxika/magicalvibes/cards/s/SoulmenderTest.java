package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Soulmender.class})
class SoulmenderTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability gains 1 life and taps Soulmender")
    void abilityGainsLife() {
        Permanent soulmender = addCreatureReady(player1, new Soulmender());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
        assertThat(soulmender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot be activated while tapped")
    void cannotActivateWhileTapped() {
        Permanent soulmender = addCreatureReady(player1, new Soulmender());
        soulmender.tap();
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent soulmender = harness.addToBattlefieldAndReturn(player1, new Soulmender());
        soulmender.setSummoningSick(true);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(soulmender.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, startingLife);
    }

    @Test
    @DisplayName("Tap cost is paid immediately but life is gained only on resolution")
    void lifeGainUsesTheStack() {
        Permanent soulmender = addCreatureReady(player1, new Soulmender());
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(soulmender.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, startingLife);

        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 1);
        harness.assertLife(player2, opponentLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability resolves after Soulmender leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent soulmender = addCreatureReady(player1, new Soulmender());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(soulmender);
        gd.playerGraveyards.get(player1.getId()).add(soulmender.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 1);
        assertThat(gd.stack).isEmpty();
    }
}
