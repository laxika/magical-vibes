package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RithsGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps Rith's Grove")
    void acceptsEtbCostByReturningNonLairLand() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Rith's Grove");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Rith's Grove is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        harness.addToBattlefield(player1, new RithsGrove());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Rith's Grove")))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Rith's Grove")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices Rith's Grove")
    void decliningEtbCostSacrificesSource() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Rith's Grove");
        harness.assertInGraveyard(player1, "Rith's Grove");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("The mana ability offers red, green, and white")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new RithsGrove());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Rith's Grove")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new RithsGrove());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new RithsGrove()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
