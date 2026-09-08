package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrosissCatacombsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps Crosis's Catacombs")
    void acceptsEtbCostByReturningNonLairLand() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Crosis's Catacombs");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Crosis's Catacombs is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        harness.addToBattlefield(player1, new CrosissCatacombs());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Crosis's Catacombs")))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Crosis's Catacombs")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices Crosis's Catacombs")
    void decliningEtbCostSacrificesSource() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Crosis's Catacombs");
        harness.assertInGraveyard(player1, "Crosis's Catacombs");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("The mana ability offers blue, black, and red")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new CrosissCatacombs());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "BLACK", "RED");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Crosis's Catacombs")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent catacombs = harness.addToBattlefieldAndReturn(player1, new CrosissCatacombs());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(catacombs.isTapped()).isTrue();
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new CrosissCatacombs()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
