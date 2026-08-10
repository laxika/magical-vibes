package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TowerOfMurmursTest extends BaseCardTest {

    @Test
    @DisplayName("Activating mills eight cards from the target player's library")
    void millsEightCardsFromTargetPlayer() {
        Permanent tower = addReadyTower(player1);
        addEightMana(player1);
        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(deck).hasSize(deckSizeBefore - 8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target yourself to mill")
    void canTargetYourself() {
        addReadyTower(player1);
        addEightMana(player1);
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(deck).hasSize(deckSizeBefore - 8);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Cannot activate without eight mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyTower(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent tower = addReadyTower(player1);
        tower.tap();
        addEightMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyTower(Player player) {
        TowerOfMurmurs card = new TowerOfMurmurs();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addEightMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 8);
    }
}
