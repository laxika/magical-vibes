package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChainflingerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to any target")
    void dealsOneDamage() {
        harness.setLife(player2, 20);
        Permanent chainflinger = addReadyChainflinger(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(chainflinger.isTapped()).isTrue();
        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Threshold ability deals 2 damage with seven cards in the graveyard")
    void thresholdDealsTwoDamage() {
        harness.setLife(player2, 20);
        Permanent chainflinger = addReadyChainflinger(player1);
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(chainflinger.isTapped()).isTrue();
        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate the threshold ability with fewer than seven cards in the graveyard")
    void cannotActivateThresholdWithoutSevenCards() {
        addReadyChainflinger(player1);
        harness.setGraveyard(player1, cards(6));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    private Permanent addReadyChainflinger(Player player) {
        Permanent permanent = new Permanent(new Chainflinger());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Chainflinger());
        }
        return cards;
    }
}
