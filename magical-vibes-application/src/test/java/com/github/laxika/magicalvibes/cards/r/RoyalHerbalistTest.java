package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoyalHerbalistTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} and exiling the top library card gains 1 life")
    void activateExilesTopAndGainsLife() {
        harness.addToBattlefield(player1, new RoyalHerbalist());
        GameData gd = harness.getGameData();
        Permanent herbalist = gd.playerBattlefields.get(player1.getId()).getFirst();

        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int exileBefore = gd.exiledCards.size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.exiledCards).hasSize(exileBefore + 1);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId()).contains(topCard.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(herbalist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with empty library")
    void cannotActivateWithEmptyLibrary() {
        harness.addToBattlefield(player1, new RoyalHerbalist());
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    @Test
    @DisplayName("Cannot activate without paying {2}")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new RoyalHerbalist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
