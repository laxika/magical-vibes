package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtravagantSpirit.class, FreshVolunteers.class})
class ExtravagantSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Paying one generic mana for each card in hand keeps Extravagant Spirit")
    void paysForCardsInHand() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new ExtravagantSpirit());
        harness.setHand(player1, List.of(new FreshVolunteers(), new FreshVolunteers()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spirit);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Extravagant Spirit")
    void declinePaymentSacrificesIt() {
        harness.addToBattlefield(player1, new ExtravagantSpirit());
        harness.setHand(player1, List.of(new FreshVolunteers()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Extravagant Spirit");
        harness.assertInGraveyard(player1, "Extravagant Spirit");
    }

    @Test
    @DisplayName("With no cards in hand, paying {0} keeps Extravagant Spirit")
    void noCardsInHandCostsNothing() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new ExtravagantSpirit());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spirit);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Extravagant Spirit")
    void acceptWithoutEnoughManaSacrificesIt() {
        harness.addToBattlefield(player1, new ExtravagantSpirit());
        harness.setHand(player1, List.of(new FreshVolunteers()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Extravagant Spirit");
        harness.assertInGraveyard(player1, "Extravagant Spirit");
    }

    @Test
    @DisplayName("The upkeep cost counts only cards in its controller's hand")
    void opponentHandDoesNotIncreaseCost() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new ExtravagantSpirit());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new FreshVolunteers(), new FreshVolunteers()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spirit);
    }

    @Test
    @DisplayName("The upkeep cost uses the hand size when the ability resolves")
    void handSizeIsEvaluatedAtResolution() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new ExtravagantSpirit());
        harness.setHand(player1, List.of(new FreshVolunteers(), new FreshVolunteers()));

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spirit);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Extravagant Spirit does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new ExtravagantSpirit());
        harness.setHand(player1, List.of(new FreshVolunteers()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Extravagant Spirit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
