package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathCultist.class, Forest.class})
class DeathCultistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Death Cultist makes the targeted player lose 1 life and its controller gain 1 life")
    void drainsTargetPlayer() {
        addReadyDeathCultist();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Death Cultist");
    }

    @Test
    @DisplayName("Death Cultist can target its controller")
    void canTargetController() {
        addReadyDeathCultist();
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death Cultist is sacrificed when its ability is activated")
    void sacrificeHappensOnActivation() {
        addReadyDeathCultist();

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Death Cultist");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death Cultist cannot target a permanent")
    void cannotTargetPermanent() {
        addReadyDeathCultist();
        var land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    private void addReadyDeathCultist() {
        addCreatureReady(player1, new DeathCultist());
    }

    @Test
    @DisplayName("Death Cultist can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new DeathCultist());
        permanent.setSummoningSick(true);
        permanent.tap();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Death Cultist");
    }

    @Test
    @DisplayName("Self-targeting at 1 life does not lose the game during resolution")
    void selfTargetAtOneLifeSurvivesResolution() {
        addReadyDeathCultist();
        harness.setLife(player1, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Death Cultist");
    }
}
