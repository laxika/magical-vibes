package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatstafArsonistsTest extends BaseCardTest {

    @Test
    void transformsWhenNoSpellsWereCastLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolve(player1);

        assertThat(arsonists.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformWhenAPlayerCastOneSpellLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeepFor(player1);

        assertThat(arsonists.isTransformed()).isFalse();
    }

    @Test
    void transformsBackWhenAPlayerCastTwoSpellsLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(arsonists.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeepAndResolve(player2);

        assertThat(arsonists.isTransformed()).isFalse();
    }

    @Test
    void doesNotTransformBackWhenEachPlayerCastOnlyOneSpellLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(arsonists.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        advanceToUpkeepFor(player2);

        assertThat(arsonists.isTransformed()).isTrue();
    }

    private Permanent addArsonists() {
        harness.addToBattlefield(player1, new GatstafArsonists());
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void advanceToUpkeepFor(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToUpkeepAndResolve(Player activePlayer) {
        advanceToUpkeepFor(activePlayer);
        harness.passBothPriorities();
    }
}
