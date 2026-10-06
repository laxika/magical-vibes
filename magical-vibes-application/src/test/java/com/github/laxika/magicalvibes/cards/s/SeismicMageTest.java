package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicMage.class, Forest.class, Mountain.class})
class SeismicMageTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{R}, {T}, Discard: destroys target land")
    void destroysTargetLand() {
        Permanent mage = addCreatureReady(player1, new SeismicMage());
        harness.addToBattlefield(player2, new Forest());
        prepareActivation();
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, null, targetId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Can destroy a land it controls")
    void destroysOwnLand() {
        Permanent mage = addCreatureReady(player1, new SeismicMage());
        harness.addToBattlefield(player1, new Forest());
        prepareActivation();
        UUID targetId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, targetId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addCreatureReady(player1, new SeismicMage());
        Permanent creature = addCreatureReady(player2, new SeismicMage());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new SeismicMage());
        harness.addToBattlefield(player2, new Forest());
        addMana();
        harness.setHand(player1, List.of());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can discard a nonland card, paying the cost before resolution")
    void discardsNonlandAsActivationCost() {
        Permanent mage = addCreatureReady(player1, new SeismicMage());
        harness.addToBattlefield(player2, new Forest());
        addMana();
        harness.setHand(player1, List.of(new SeismicMage()));
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, null, targetId);
        harness.handleCardChosen(player1, 0);

        assertThat(mage.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Seismic Mage");
        harness.assertInGraveyard(player1, "Seismic Mage");
        harness.assertOnBattlefield(player2, "Forest");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player1, "Seismic Mage");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent mage = addCreatureReady(player1, new SeismicMage());
        mage.setTapped(true);
        harness.addToBattlefield(player2, new Forest());
        prepareActivation();
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mage = addCreatureReady(player1, new SeismicMage());
        mage.setSummoningSick(true);
        harness.addToBattlefield(player2, new Forest());
        prepareActivation();
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new SeismicMage());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Mountain()));
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
    }
    private void prepareActivation() {
        addMana();
        harness.setHand(player1, List.of(new Mountain()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
