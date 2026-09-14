package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Aquamoeba;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Compulsion.class, Aquamoeba.class})
class CompulsionTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card draws a card")
    void discardingCardDrawsCard() {
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new Aquamoeba()));
        harness.setLibrary(player1, List.of(new Compulsion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Compulsion");
        harness.assertInGraveyard(player1, "Aquamoeba");
    }

    @Test
    @DisplayName("Sacrificing Compulsion draws a card")
    void sacrificingCompulsionDrawsCard() {
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Aquamoeba()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Aquamoeba");
        harness.assertNotOnBattlefield(player1, "Compulsion");
        harness.assertInGraveyard(player1, "Compulsion");
    }

    @Test
    @DisplayName("The discard ability cannot be activated with an empty hand")
    void discardAbilityRequiresCardInHand() {
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both abilities require blue mana")
    void abilitiesRequireBlueMana() {
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new Aquamoeba()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Compulsion");
    }
}
