package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindsAglow.class, Island.class})
class MindsAglowTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may pay mana and all players draw the total")
    void eachPlayerPaysAndAllDrawTheTotal() {
        harness.setHand(player1, List.of(new MindsAglow()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 1);
        int player1HandBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castSorcery(player1, 0);
        harness.forceActivePlayer(player2);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Zero payments produce no draws and players without mana are skipped")
    void zeroPaymentsProduceNoDraws() {
        harness.setHand(player1, List.of(new MindsAglow()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int player1HandBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A player declining payment still draws the full total")
    void decliningPlayerStillDraws() {
        harness.setHand(player1, List.of(new MindsAglow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Players may decline payment even when they have mana")
    void bothPlayersDeclineWithManaAvailable() {
        harness.setHand(player1, List.of(new MindsAglow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent can contribute when the caster has no mana left")
    void opponentPaysWhenCasterHasNoMana() {
        harness.setHand(player1, List.of(new MindsAglow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player2, 3);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Players can tap mana sources while deciding their contribution")
    void canGenerateManaDuringResolution() {
        harness.setHand(player1, List.of(new MindsAglow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.tapPermanent(gd, player1, 0);
        harness.handleXValueChosen(player1, 1);
        gs.tapPermanent(gd, player2, 0);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
