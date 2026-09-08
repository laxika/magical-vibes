package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConvictedKillerTest extends BaseCardTest {

    @Test
    void transformsWhenNoSpellsWereCastLastTurn() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();

        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(permanent.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformWhenAPlayerCastOneSpellLastTurn() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(permanent.isTransformed()).isFalse();
    }

    @Test
    void transformsBackWhenAPlayerCastTwoSpellsLastTurn() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(permanent.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(permanent.isTransformed()).isFalse();
    }

    @Test
    void doesNotTransformBackWhenPlayersCastOneSpellEachLastTurn() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(permanent.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(permanent.isTransformed()).isTrue();
    }

    private Permanent addConvictedKiller() {
        harness.addToBattlefield(player1, new ConvictedKiller());
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
