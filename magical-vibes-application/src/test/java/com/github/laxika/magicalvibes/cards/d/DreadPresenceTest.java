package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreadPresenceTest extends BaseCardTest {

    private static final String DRAW = "You draw a card and you lose 1 life.";
    private static final String DAMAGE = "This creature deals 2 damage to any target and you gain 2 life.";

    @Test
    @DisplayName("Swamp landfall draws a card and loses 1 life when that mode is chosen")
    void drawMode() {
        harness.addToBattlefield(player1, new DreadPresence());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Swamp landfall damages any target and gains 2 life when that mode is chosen")
    void damageMode() {
        harness.addToBattlefield(player1, new DreadPresence());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A non-Swamp land does not trigger Dread Presence")
    void nonSwampDoesNotTrigger() {
        harness.addToBattlefield(player1, new DreadPresence());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
