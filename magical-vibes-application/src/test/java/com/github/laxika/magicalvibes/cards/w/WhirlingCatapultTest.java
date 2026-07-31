package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhirlingCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature with flying and each player, exiling two cards")
    void damagesFliersAndPlayers() {
        harness.addToBattlefield(player1, new WhirlingCatapult());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GameData gd = harness.getGameData();

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int exileBefore = gd.exiledCards.size();
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
        assertThat(gd.exiledCards).hasSize(exileBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1 - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 - 1);

        // Both 1/1 fliers die; the non-flying 2/2 is untouched.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SuntailHawk);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard() instanceof SuntailHawk)
                .anyMatch(p -> p.getCard() instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Cannot activate with fewer than two cards in library")
    void cannotActivateWithShortLibrary() {
        harness.addToBattlefield(player1, new WhirlingCatapult());
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying {2}")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new WhirlingCatapult());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
