package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PenregonStrongbull.class, EnergyRefractor.class})
class PenregonStrongbullTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact boosts Penregon Strongbull and damages each opponent")
    void sacrificeArtifactBoostsAndDamagesOpponent() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent bull = findPermanent(player1, "Penregon Strongbull");
        int playerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Energy Refractor");
        assertThat(bull.getPowerModifier()).isEqualTo(1);
        assertThat(bull.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(playerLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent bull = findPermanent(player1, "Penregon Strongbull");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isEqualTo(0);
        assertThat(bull.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("The artifact is sacrificed as a cost before the boost and damage resolve")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent bull = findPermanent(player1, "Penregon Strongbull");
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Energy Refractor");
        harness.assertInGraveyard(player1, "Energy Refractor");
        assertThat(gd.stack).hasSize(1);
        assertThat(bull.getPowerModifier()).isZero();
        assertThat(bull.getToughnessModifier()).isZero();
        harness.assertLife(player2, opponentLife);

        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isEqualTo(1);
        assertThat(bull.getToughnessModifier()).isEqualTo(1);
        harness.assertLife(player2, opponentLife - 1);
    }

    @Test
    @DisplayName("Repeated activations accumulate boosts and damage without tapping the source")
    void repeatedActivationsAccumulate() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent bull = findPermanent(player1, "Penregon Strongbull");
        int opponentLife = gd.getLife(player2.getId());

        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isEqualTo(2);
        assertThat(bull.getToughnessModifier()).isEqualTo(2);
        assertThat(bull.isTapped()).isFalse();
        harness.assertLife(player2, opponentLife - 2);
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addToBattlefield(player2, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Energy Refractor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability requires mana as well as an artifact")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addToBattlefield(player1, new EnergyRefractor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Energy Refractor");
        assertThat(gd.stack).isEmpty();
    }
}
