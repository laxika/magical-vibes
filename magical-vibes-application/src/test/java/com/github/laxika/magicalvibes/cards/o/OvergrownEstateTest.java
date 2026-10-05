package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvergrownEstate.class, CavesOfKoilos.class})
class OvergrownEstateTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gains 3 life")
    void sacrificeLandGainsThreeLife() {
        harness.addToBattlefield(player1, new OvergrownEstate());
        harness.addToBattlefieldAndReturn(player1, new CavesOfKoilos());

        prepareAbilityActivation();
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Caves of Koilos");
    }

    @Test
    @DisplayName("With multiple lands the controller chooses which land to sacrifice")
    void promptsForLandChoice() {
        harness.addToBattlefield(player1, new OvergrownEstate());
        harness.addToBattlefieldAndReturn(player1, new CavesOfKoilos());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CavesOfKoilos());

        prepareAbilityActivation();
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(second);
    }

    @Test
    @DisplayName("Cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new OvergrownEstate());

        prepareAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new OvergrownEstate());
        harness.addToBattlefield(player2, new CavesOfKoilos());

        prepareAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Caves of Koilos");
    }

    @Test
    @DisplayName("A tapped land is sacrificed immediately but life is gained only on resolution")
    void tappedLandIsPaidBeforeLifeGainResolves() {
        harness.addToBattlefield(player1, new OvergrownEstate());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CavesOfKoilos());
        land.setTapped(true);
        prepareAbilityActivation();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Caves of Koilos");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The ability can be activated twice during the opponent's turn")
    void repeatedActivationOnOpponentsTurn() {
        Permanent estate = harness.addToBattlefieldAndReturn(player1, new OvergrownEstate());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new CavesOfKoilos());
        harness.addToBattlefield(player1, new CavesOfKoilos());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(estate);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
        assertThat(estate.isTapped()).isFalse();
    }
    private void prepareAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
