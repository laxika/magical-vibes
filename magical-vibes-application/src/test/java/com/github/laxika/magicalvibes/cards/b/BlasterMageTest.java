package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WallOfDistortion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlasterMage.class, WallOfDistortion.class})
class BlasterMageTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T}, Discard: destroys target Wall")
    void destroysTargetWall() {
        Permanent mage = addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        prepareActivation();

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Wall of Distortion");
        harness.assertInGraveyard(player1, "Blaster Mage");
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        addCreatureReady(player1, new BlasterMage());
        Permanent creature = addCreatureReady(player2, new BlasterMage());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new BlasterMage());
        Permanent wall = addCreatureReady(player2, new WallOfDistortion());
        harness.setHand(player1, List.of(new BlasterMage()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareActivation() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new BlasterMage()));
    }
}
