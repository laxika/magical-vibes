package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HopeOfGhirapur;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbraalGearSmasher.class, HopeOfGhirapur.class})
class EmbraalGearSmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing an artifact deals 2 damage to each opponent")
    void tappingAndSacrificingArtifactDealsDamageToEachOpponent() {
        Permanent smasher = addCreatureReady(player1, new EmbraalGearSmasher());
        harness.addToBattlefield(player1, new HopeOfGhirapur());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hope of Ghirapur");
        assertThat(smasher.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addCreatureReady(player1, new EmbraalGearSmasher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        addCreatureReady(player1, new EmbraalGearSmasher());
        harness.addToBattlefield(player2, new HopeOfGhirapur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
        harness.assertOnBattlefield(player2, "Hope of Ghirapur");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new EmbraalGearSmasher());
        harness.addToBattlefield(player1, new HopeOfGhirapur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Hope of Ghirapur");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent smasher = addCreatureReady(player1, new EmbraalGearSmasher());
        smasher.tap();
        harness.addToBattlefield(player1, new HopeOfGhirapur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertOnBattlefield(player1, "Hope of Ghirapur");
    }

    @Test
    void tappedArtifactIsSacrificedBeforeDamageResolves() {
        Permanent smasher = addCreatureReady(player1, new EmbraalGearSmasher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HopeOfGhirapur());
        artifact.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Hope of Ghirapur");
        assertThat(smasher.isTapped()).isTrue();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
