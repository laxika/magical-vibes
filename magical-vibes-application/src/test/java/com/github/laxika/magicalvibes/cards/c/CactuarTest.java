package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Cactuar.class)
class CactuarTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand at its controller's end step if it entered earlier")
    void returnsItselfWhenItDidNotEnterThisTurn() {
        addCactuar(player1);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cactuar");
        harness.assertInHand(player1, "Cactuar");
    }

    @Test
    @DisplayName("Does not return itself if it entered the battlefield this turn")
    void doesNotReturnWhenItEnteredThisTurn() {
        Permanent cactuar = addCactuar(player1);
        gd.permanentsEnteredBattlefieldThisTurn.put(
                player1.getId(), new ArrayList<>(List.of(cactuar.getCard())));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cactuar");
        harness.assertNotInHand(player1, "Cactuar");
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        addCactuar(player1);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cactuar");
    }

    private Permanent addCactuar(Player player) {
        Permanent cactuar = new Permanent(new Cactuar());
        cactuar.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(cactuar);
        return cactuar;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
