package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WavebreakHippocamp.class, Shock.class})
class WavebreakHippocampTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell during an opponent's turn draws a card")
    void firstSpellDuringOpponentsTurnDrawsCard() {
        addHippocampAndEnterOpponentsTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Only the first spell during an opponent's turn draws a card")
    void onlyFirstSpellDrawsCard() {
        addHippocampAndEnterOpponentsTurn();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Casting during your own turn does not draw a card")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new WavebreakHippocamp());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    private void addHippocampAndEnterOpponentsTurn() {
        harness.addToBattlefield(player1, new WavebreakHippocamp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
