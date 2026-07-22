package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HinterlandLoggerTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Timber Shredder when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandLogger());
        Permanent logger = findPermanent(player1, "Hinterland Logger");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(logger.isTransformed()).isTrue();
        assertThat(logger.getCard().getName()).isEqualTo("Timber Shredder");
        assertThat(gqs.getEffectivePower(gd, logger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, logger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandLogger());
        Permanent logger = findPermanent(player1, "Hinterland Logger");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(logger.isTransformed()).isFalse();
        assertThat(logger.getCard().getName()).isEqualTo("Hinterland Logger");
    }

    @Test
    @DisplayName("Timber Shredder transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandLogger());
        Permanent logger = findPermanent(player1, "Hinterland Logger");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(logger.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(logger.isTransformed()).isFalse();
        assertThat(logger.getCard().getName()).isEqualTo("Hinterland Logger");
        assertThat(gqs.getEffectivePower(gd, logger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, logger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Timber Shredder does not transform back when only one spell was cast last turn")
    void doesNotTransformBackWithOnlyOneSpellCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandLogger());
        Permanent logger = findPermanent(player1, "Hinterland Logger");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(logger.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(logger.isTransformed()).isTrue();
        assertThat(logger.getCard().getName()).isEqualTo("Timber Shredder");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new HinterlandLogger());
        Permanent logger = findPermanent(player1, "Hinterland Logger");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(logger.isTransformed()).isTrue();
        assertThat(logger.getCard().getName()).isEqualTo("Timber Shredder");
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
