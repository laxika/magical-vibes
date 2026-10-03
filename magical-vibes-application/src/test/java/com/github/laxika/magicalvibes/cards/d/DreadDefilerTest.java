package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.cards.o.OblivionStrike;
import com.github.laxika.magicalvibes.cards.s.SlaughterDrone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadDefiler.class, GraspOfDarkness.class,
        OblivionStrike.class, SlaughterDrone.class})
class DreadDefilerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and makes an opponent lose life equal to its power")
    void exilesCreatureAndLosesLifeEqualToItsPower() {
        Permanent dreadDefiler = harness.addToBattlefieldAndReturn(player1, new DreadDefiler());
        dreadDefiler.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new SlaughterDrone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotInGraveyard(player1, "Slaughter Drone");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Slaughter Drone"));
    }

    @Test
    @DisplayName("Cannot target the ability's controller")
    void cannotTargetController() {
        Permanent dreadDefiler = harness.addToBattlefieldAndReturn(player1, new DreadDefiler());
        dreadDefiler.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new SlaughterDrone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        harness.assertInGraveyard(player1, "Slaughter Drone");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        Permanent dreadDefiler = harness.addToBattlefieldAndReturn(player1, new DreadDefiler());
        dreadDefiler.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void stackedActivationsUseTheirOwnExiledCardsPower() {
        harness.addToBattlefield(player1, new DreadDefiler());
        harness.setGraveyard(player1, List.of(new SlaughterDrone(), new DreadDefiler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.passBothPriorities();
        harness.assertLife(player2, 12);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void canActivateWhileSummoningSickAndTappedWithMixedMana() {
        Permanent defiler = harness.addToBattlefieldAndReturn(player1, new DreadDefiler());
        defiler.setSummoningSick(true);
        defiler.setTapped(true);
        harness.setGraveyard(player1, List.of(new SlaughterDrone()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void coloredManaCannotPayTheColorlessRequirement() {
        harness.addToBattlefield(player1, new DreadDefiler());
        harness.setGraveyard(player1, List.of(new SlaughterDrone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> {
            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.handleGraveyardCardChosen(player1, 0);
        }).isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Slaughter Drone");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void noncreatureCardsAndOpponentsGraveyardCannotPayTheCost() {
        harness.addToBattlefield(player1, new DreadDefiler());
        harness.setGraveyard(player1, List.of(new OblivionStrike()));
        harness.setGraveyard(player2, List.of(new SlaughterDrone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Oblivion Strike");
        harness.assertInGraveyard(player2, "Slaughter Drone");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void abilityStillResolvesAfterSourceDies() {
        Permanent defiler = harness.addToBattlefieldAndReturn(player1, new DreadDefiler());
        harness.setGraveyard(player1, List.of(new SlaughterDrone()));
        harness.setHand(player2, List.of(new GraspOfDarkness(), new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.castAndResolveInstant(player2, 0, defiler.getId());
        harness.castAndResolveInstant(player2, 0, defiler.getId());
        harness.assertNotOnBattlefield(player1, "Dread Defiler");
        harness.assertInGraveyard(player1, "Dread Defiler");

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void usesLastKnownPowerWhenExiledCardChangesZonesBeforeResolution() {
        harness.addToBattlefield(player1, new DreadDefiler());
        SlaughterDrone drone = new SlaughterDrone();
        harness.setGraveyard(player1, List.of(drone));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.setExile(player1, List.of());
        harness.setGraveyard(player1, List.of(drone));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Slaughter Drone");
    }
}
