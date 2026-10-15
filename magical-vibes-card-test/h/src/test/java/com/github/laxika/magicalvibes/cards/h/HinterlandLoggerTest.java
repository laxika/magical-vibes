package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HinterlandLogger.class})
class HinterlandLoggerTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Timber Shredder when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(logger.isTransformed()).isTrue();
        assertThat(logger.getCard().getName()).isEqualTo("Timber Shredder");
        assertThat(gqs.getEffectivePower(gd, logger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, logger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        assertThat(logger.isTransformed()).isFalse();
        assertThat(logger.getCard().getName()).isEqualTo("Hinterland Logger");
    }

    @Test
    @DisplayName("Timber Shredder transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(logger.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(logger.isTransformed()).isFalse();
        assertThat(logger.getCard().getName()).isEqualTo("Hinterland Logger");
        assertThat(gqs.getEffectivePower(gd, logger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, logger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Timber Shredder does not transform back when only one spell was cast last turn")
    void doesNotTransformBackWithOnlyOneSpellCastLastTurn() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(logger.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        assertThat(logger.isTransformed()).isTrue();
        assertThat(logger.getCard().getName()).isEqualTo("Timber Shredder");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(logger.isTransformed()).isTrue();
        assertThat(logger.getCard().getName()).isEqualTo("Timber Shredder");
    }

    @Test
    @DisplayName("A spell cast by the opponent prevents the front face from transforming")
    void opponentSpellPreventsTransformation() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(logger.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Timber Shredder stays transformed through another spell-free turn")
    void backFaceRemainsWhenNoSpellsWereCast() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(logger.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(logger.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Timber Shredder transforms back when its controller cast more than two spells")
    void controllerCastingThreeSpellsTransformsBack() {
        Permanent logger = harness.addToBattlefieldAndReturn(player1, new HinterlandLogger());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(logger.isTransformed()).isTrue();
        gd.spellsCastLastTurn.put(player1.getId(), 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(logger.isTransformed()).isFalse();
    }
}
