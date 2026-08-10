package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TowerOfFortunesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {8} and tapping Tower of Fortunes draws four cards")
    void drawsFourCards() {
        addTower(player1);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.forceActivePlayer(player1);
        Permanent tower = gd.playerBattlefields.get(player1.getId()).getFirst();
        tower.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate Tower of Fortunes without enough mana")
    void requiresEightMana() {
        addTower(player1);
        harness.forceActivePlayer(player1);
        Permanent tower = gd.playerBattlefields.get(player1.getId()).getFirst();
        tower.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tower.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate Tower of Fortunes while tapped")
    void cannotActivateWhileTapped() {
        addTower(player1);
        harness.forceActivePlayer(player1);
        Permanent tower = gd.playerBattlefields.get(player1.getId()).getFirst();
        tower.setSummoningSick(false);
        tower.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addTower(Player player) {
        Permanent tower = new Permanent(new TowerOfFortunes());
        tower.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tower);
        return tower;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
