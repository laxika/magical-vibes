package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CreosoteHeath.class)
class CreosoteHeathTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and deals 1 damage to target opponent")
    void entersTappedAndDamagesTargetOpponent() {
        harness.setHand(player1, List.of(new CreosoteHeath()));
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
    @DisplayName("Tapping produces green mana")
    void tappingProducesGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CreosoteHeath());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces white mana")
    void tappingProducesWhiteMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CreosoteHeath());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped land cannot produce either color of mana")
    void tappedLandCannotProduceMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CreosoteHeath());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("The damage trigger resolves even after the land leaves the battlefield")
    void damageTriggerResolvesWithoutSource() {
        harness.setHand(player1, List.of(new CreosoteHeath()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        Permanent land = findPermanent(player1, "Creosote Heath");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Creosote Heath");
        harness.assertInGraveyard(player1, "Creosote Heath");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
