package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShivanHarvest.class, RagingKavu.class, ShivanOasis.class, Forest.class})
class ShivanHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and destroys target nonbasic land")
    void sacrificesCreatureAndDestroysNonbasicLand() {
        harness.addToBattlefield(player1, new ShivanHarvest());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player2, new ShivanOasis());
        addMana();
        UUID targetId = harness.getPermanentId(player2, "Shivan Oasis");

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.assertInGraveyard(player1, "Raging Kavu");
        harness.assertOnBattlefield(player2, "Shivan Oasis");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shivan Oasis");
    }

    @Test
    @DisplayName("Can destroy an own nonbasic land")
    void canDestroyOwnNonbasicLand() {
        harness.addToBattlefield(player1, new ShivanHarvest());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player1, new ShivanOasis());
        addMana();
        UUID targetId = harness.getPermanentId(player1, "Shivan Oasis");

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raging Kavu");
        harness.assertInGraveyard(player1, "Shivan Oasis");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new ShivanHarvest());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player2, new Forest());
        addMana();
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Raging Kavu");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new ShivanHarvest());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player2, new RagingKavu());
        addMana();
        UUID targetId = harness.getPermanentId(player2, "Raging Kavu");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Raging Kavu");
        harness.assertOnBattlefield(player2, "Raging Kavu");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new ShivanHarvest());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player2, new ShivanOasis());
        UUID targetId = harness.getPermanentId(player2, "Shivan Oasis");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new ShivanHarvest());
        harness.addToBattlefield(player2, new ShivanOasis());
        addMana();
        UUID targetId = harness.getPermanentId(player2, "Shivan Oasis");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
