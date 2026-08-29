package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HurkylsFinalMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all nonland permanents to their owners' hands, then ends the turn")
    void returnsNonlandsAndEndsTurn() {
        GrizzlyBears player1Bears = new GrizzlyBears();
        Forest player1Forest = new Forest();
        GrizzlyBears player2Bears = new GrizzlyBears();
        Forest player2Forest = new Forest();
        HurkylsFinalMeditation spell = new HurkylsFinalMeditation();
        harness.addToBattlefield(player1, player1Bears);
        harness.addToBattlefield(player1, player1Forest);
        harness.addToBattlefield(player2, player2Bears);
        harness.addToBattlefield(player2, player2Forest);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Bears);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == player1Forest);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == player2Forest);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.activePlayerId).isNotEqualTo(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Costs {3} more to cast during another player's turn")
    void costsMoreOnAnotherPlayersTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new HurkylsFinalMeditation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
