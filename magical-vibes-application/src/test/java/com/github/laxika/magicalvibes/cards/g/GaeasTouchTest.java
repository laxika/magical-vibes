package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CloudcrestLake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaeasTouch.class, Forest.class, Island.class, CloudcrestLake.class})
class GaeasTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a basic Forest from hand onto the battlefield")
    void putsBasicForestFromHand() {
        addTouch();
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only basic Forest cards are valid choices")
    void onlyBasicForestsAreValidChoices() {
        addTouch();
        harness.setHand(player1, List.of(new Forest(), new Island(), new CloudcrestLake()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("The Forest ability can be activated only once each turn")
    void forestAbilityIsLimitedToOnceEachTurn() {
        addTouch();
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("each turn");
    }

    @Test
    @DisplayName("The Forest ability requires sorcery speed")
    void forestAbilityRequiresSorcerySpeed() {
        addTouch();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Sacrificing Gaea's Touch adds two green mana")
    void sacrificeAbilityAddsTwoGreenMana() {
        addTouch();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Gaea's Touch");
        harness.assertInGraveyard(player1, "Gaea's Touch");
    }

    private Permanent addTouch() {
        return harness.addToBattlefieldAndReturn(player1, new GaeasTouch());
    }
}
