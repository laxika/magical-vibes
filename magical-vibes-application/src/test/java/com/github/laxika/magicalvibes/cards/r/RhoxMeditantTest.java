package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RhoxMeditantTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card on enter when controlling a green permanent")
    void drawsWhenControllingGreenPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // green permanent
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rhox Meditant"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // drew a card
    }

    @Test
    @DisplayName("Does not draw when controlling no green permanent")
    void noDrawWithoutGreenPermanent() {
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve (conditional no-op) ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rhox Meditant"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty(); // no draw
    }

    @Test
    @DisplayName("Does not draw when only the opponent controls a green permanent")
    void noDrawWhenOpponentControlsGreenPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // opponent's green permanent
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve (conditional no-op) ETB trigger

        assertThat(gd.playerHands.get(player1.getId())).isEmpty(); // "you control" fails
    }
}
