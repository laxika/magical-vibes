package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DromarsCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps Dromar's Cavern")
    void acceptsEtbCostByReturningNonLairLand() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Dromar's Cavern");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Dromar's Cavern is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        harness.addToBattlefield(player1, new DromarsCavern());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Dromar's Cavern")))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Dromar's Cavern")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices Dromar's Cavern")
    void decliningEtbCostSacrificesSource() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Dromar's Cavern");
        harness.assertInGraveyard(player1, "Dromar's Cavern");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("The mana ability offers white, blue, and black")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new DromarsCavern());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Dromar's Cavern")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new DromarsCavern());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(cavern.isTapped()).isTrue();
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new DromarsCavern()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
