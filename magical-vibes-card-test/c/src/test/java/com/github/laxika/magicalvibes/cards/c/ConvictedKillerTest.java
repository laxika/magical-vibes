package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConvictedKiller.class})
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

    @Test
    void transformsDuringOpponentsUpkeepWhenNoSpellsWereCast() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);

        assertThat(permanent.isTransformed()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(permanent.isTransformed()).isTrue();
    }

    @Test
    void doesNotTriggerWhenOnlyOpponentCastASpellLastTurn() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(permanent.isTransformed()).isFalse();
    }

    @Test
    void remainsTransformedWhenNoSpellsWereCastLastTurn() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(permanent.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(permanent.isTransformed()).isTrue();
    }

    @Test
    void remainsTransformedWhenOnePlayerCastOnlyOneSpellLastTurn() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(permanent.isTransformed()).isTrue();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(permanent.isTransformed()).isTrue();
    }

    @Test
    void transformsBackDuringControllersUpkeepWhenControllerCastThreeSpells() {
        Permanent permanent = addConvictedKiller();
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);
        assertThat(permanent.isTransformed()).isTrue();
        gd.spellsCastLastTurn.put(player1.getId(), 3);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(permanent.isTransformed()).isTrue();
        harness.passBothPriorities();
        assertThat(permanent.isTransformed()).isFalse();
    }

    private Permanent addConvictedKiller() {
        return harness.addToBattlefieldAndReturn(player1, new ConvictedKiller());
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }
}
