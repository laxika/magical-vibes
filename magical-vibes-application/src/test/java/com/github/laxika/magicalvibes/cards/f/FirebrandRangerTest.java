package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
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

@CardUsed({FirebrandRanger.class, CoastalTower.class, Forest.class})
class FirebrandRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Firebrand Ranger taps it and pays green mana")
    void activatingTapsAndPaysGreenMana() {
        Permanent ranger = addCreatureReady(player1, new FirebrandRanger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ranger.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Resolving the ability presents a may choice")
    void resolvingPresentsMayChoice() {
        addCreatureReady(player1, new FirebrandRanger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Cannot activate while Firebrand Ranger has summoning sickness")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new FirebrandRanger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing a basic land puts it onto the battlefield untapped")
    void choosingBasicLandPutsItUntapped() {
        addCreatureReady(player1, new FirebrandRanger());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only basic lands are valid hand choices")
    void onlyBasicLandsAreValidChoices() {
        addCreatureReady(player1, new FirebrandRanger());
        harness.setHand(player1, List.of(new CoastalTower(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Accepting without a basic land in hand does nothing")
    void acceptingWithoutBasicLandDoesNothing() {
        addCreatureReady(player1, new FirebrandRanger());
        harness.setHand(player1, List.of(new CoastalTower()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Coastal Tower");
        harness.assertNotOnBattlefield(player1, "Coastal Tower");
    }

    @Test
    @DisplayName("Declining the may choice leaves the basic land in hand")
    void decliningLeavesLandInHand() {
        addCreatureReady(player1, new FirebrandRanger());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new FirebrandRanger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
