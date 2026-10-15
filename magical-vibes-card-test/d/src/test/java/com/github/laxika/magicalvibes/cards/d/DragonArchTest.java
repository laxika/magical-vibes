package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.c.CoalitionFlag;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DragonArch.class, GaeasSkyfolk.class, AngelfireCrusader.class, CoalitionFlag.class, MycosynthLattice.class})
class DragonArchTest extends BaseCardTest {

    @Test
    @DisplayName("The ability offers only multicolored creature cards from hand")
    void abilityOffersOnlyMulticoloredCreatures() {
        addReadyDragonArch();
        harness.setHand(player1, List.of(new CoalitionFlag(), new AngelfireCrusader(), new GaeasSkyfolk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(2);
    }

    @Test
    @DisplayName("Choosing a multicolored creature puts it onto the battlefield untapped")
    void choosingMulticoloredCreaturePutsItOntoBattlefield() {
        Permanent arch = addReadyDragonArch();
        GaeasSkyfolk creature = new GaeasSkyfolk();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Gaea's Skyfolk");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(arch.isTapped()).isTrue();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .toList()).singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Declining the may choice leaves the hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyDragonArch();
        List<Card> hand =
                List.of(new GaeasSkyfolk(), new AngelfireCrusader(), new CoalitionFlag());
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactlyElementsOf(hand);
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("The ability cannot be activated when Dragon Arch is tapped")
    void cannotActivateWhenTapped() {
        Permanent arch = addReadyDragonArch();
        arch.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The ability cannot be activated without two mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyDragonArch();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Colorless cards under Mycosynth Lattice cannot be put onto the battlefield")
    void colorlessCardsUnderLatticeAreNotEligible() {
        Permanent arch = addReadyDragonArch();
        harness.addToBattlefield(player2, new MycosynthLattice());
        GaeasSkyfolk creature = new GaeasSkyfolk();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(arch.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability resolves with no eligible cards even when the opponent has one")
    void noEligibleCardsResolvesWithoutHandChoice() {
        Permanent arch = addReadyDragonArch();
        AngelfireCrusader creature = new AngelfireCrusader();
        harness.setHand(player1, List.of(creature));
        GaeasSkyfolk opponentCreature = new GaeasSkyfolk();
        harness.setHand(player2, List.of(opponentCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCreature);
        harness.assertNotOnBattlefield(player1, "Angelfire Crusader");
        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
        assertThat(arch.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the chosen creature enters when multiple cards are eligible")
    void putsExactlyOneChosenCreatureOntoBattlefield() {
        addReadyDragonArch();
        GaeasSkyfolk first = new GaeasSkyfolk();
        GaeasSkyfolk second = new GaeasSkyfolk();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(second.getId()))
                .hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
    }

    private Permanent addReadyDragonArch() {
        return harness.addToBattlefieldAndReturn(player1, new DragonArch());
    }
}
