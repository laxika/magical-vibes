package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacesTriumph.class, JaceWielderOfMysteries.class, GrizzlyBears.class})
class JacesTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards without a Jace planeswalker")
    void drawsTwoWithoutJace() {
        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws three cards while controlling a Jace planeswalker")
    void drawsThreeWithJace() {
        addJace(player1);

        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not count an opponent's Jace planeswalker")
    void opponentJaceDoesNotCount() {
        addJace(player2);

        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void castTriumph(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new JacesTriumph()));
        harness.setLibrary(player, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);

        harness.castSorcery(player, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addJace(Player player) {
        Permanent jace = new Permanent(new JaceWielderOfMysteries());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player.getId()).add(jace);
        return jace;
    }
}
