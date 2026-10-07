package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpareSupplies.class, Forest.class})
class SpareSuppliesTest extends BaseCardTest {

    @Test
    @DisplayName("Spare Supplies enters tapped and draws a card")
    void entersTappedAndDrawsCard() {
        harness.setHand(player1, List.of(new SpareSupplies()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent supplies = findPermanent(player1, "Spare Supplies");
        assertThat(supplies.isTapped()).isTrue();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Paying two mana and tapping Spare Supplies sacrifices it and draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new SpareSupplies());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spare Supplies");
        harness.assertInGraveyard(player1, "Spare Supplies");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Spare Supplies");
        harness.assertInGraveyard(player1, "Spare Supplies");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Tapped Spare Supplies cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent supplies = harness.addToBattlefieldAndReturn(player1, new SpareSupplies());
        supplies.setTapped(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spare Supplies");
        harness.assertNotInGraveyard(player1, "Spare Supplies");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spare Supplies cannot activate with only one mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent supplies = harness.addToBattlefieldAndReturn(player1, new SpareSupplies());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(supplies.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Spare Supplies");
        harness.assertNotInGraveyard(player1, "Spare Supplies");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entry draw goes to the controller and waits for trigger resolution")
    void entryDrawUsesControllerAndTheStack() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent supplies = harness.enterBattlefieldAndReturn(player2, new SpareSupplies());

        assertThat(supplies.isTapped()).isTrue();
        harness.assertNotInHand(player2, "Forest");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

}
