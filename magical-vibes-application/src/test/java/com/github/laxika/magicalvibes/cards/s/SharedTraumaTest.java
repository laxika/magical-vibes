package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharedTrauma.class, Forest.class})
class SharedTraumaTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may pay mana and all players mill the total")
    void eachPlayerPaysAndAllPlayersMillTheTotal() {
        harness.setHand(player1, List.of(new SharedTrauma()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.forceActivePlayer(player2);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Zero payments mill nothing and players without mana are skipped")
    void zeroPaymentsMillNothing() {
        harness.setHand(player1, List.of(new SharedTrauma()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
