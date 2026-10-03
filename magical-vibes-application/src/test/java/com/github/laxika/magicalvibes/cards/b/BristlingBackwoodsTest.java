package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BristlingBackwoods.class)
class BristlingBackwoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and deals 1 damage to target opponent")
    void entersTappedAndDamagesTargetOpponent() {
        harness.setHand(player1, List.of(new BristlingBackwoods()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping produces red mana")
    void tappingProducesRedMana() {
        harness.addToBattlefield(player1, new BristlingBackwoods());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces green mana")
    void tappingProducesGreenMana() {
        harness.addToBattlefield(player1, new BristlingBackwoods());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
    @Test
    @DisplayName("Entry damage uses the stack and survives the land leaving")
    void entryDamageResolvesAfterLandLeaves() {
        harness.setHand(player1, List.of(new BristlingBackwoods()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.assertNotOnBattlefield(player1, "Bristling Backwoods");

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The other player's land targets its controller's opponent")
    void opponentRestrictionUsesLandController() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BristlingBackwoods()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.playLand(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}
