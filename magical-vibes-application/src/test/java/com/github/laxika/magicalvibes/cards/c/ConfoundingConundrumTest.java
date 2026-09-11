package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConfoundingConundrum.class, Forest.class, Mountain.class})
class ConfoundingConundrumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsCard() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new ConfoundingConundrum());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("First opponent land does not trigger")
    void firstOpponentLandDoesNotTrigger() {
        addConundrum();

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second opponent land triggers that player to return a land")
    void secondOpponentLandReturnsOpponentsLand() {
        addConundrum();
        Permanent controllerLand = harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        Permanent returnedLand = harness.enterBattlefieldAndReturn(player2, new Mountain());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, returnedLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(controllerLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getCard()).isInstanceOf(Forest.class);
        assertThat(gd.playerHands.get(player2.getId())).contains(returnedLand.getCard());
    }

    private void addConundrum() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new ConfoundingConundrum());
        harness.passBothPriorities();
    }
}
