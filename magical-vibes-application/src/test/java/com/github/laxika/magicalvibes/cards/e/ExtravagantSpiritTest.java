package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtravagantSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Paying one generic mana for each card in hand keeps Extravagant Spirit")
    void paysForCardsInHand() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new ExtravagantSpirit());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

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
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Extravagant Spirit");
        harness.assertInGraveyard(player1, "Extravagant Spirit");
    }
}
