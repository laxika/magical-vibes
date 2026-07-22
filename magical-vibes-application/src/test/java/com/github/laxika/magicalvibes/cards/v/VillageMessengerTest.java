package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VillageMessengerTest extends BaseCardTest {

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Moonrise Intruder when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new VillageMessenger());
        Permanent messenger = findPermanent(player1, "Village Messenger");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(messenger.isTransformed()).isTrue();
        assertThat(messenger.getCard().getName()).isEqualTo("Moonrise Intruder");
        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new VillageMessenger());
        Permanent messenger = findPermanent(player1, "Village Messenger");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(messenger.isTransformed()).isFalse();
        assertThat(messenger.getCard().getName()).isEqualTo("Village Messenger");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Moonrise Intruder transforms back when a player cast two or more spells last turn")
    void intruderTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new VillageMessenger());
        Permanent messenger = findPermanent(player1, "Village Messenger");

        // Transform to Moonrise Intruder first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(messenger.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(messenger.isTransformed()).isFalse();
        assertThat(messenger.getCard().getName()).isEqualTo("Village Messenger");
        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Moonrise Intruder does not transform back when only one spell was cast last turn")
    void intruderDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new VillageMessenger());
        Permanent messenger = findPermanent(player1, "Village Messenger");

        // Transform to Moonrise Intruder first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(messenger.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(messenger.isTransformed()).isTrue();
        assertThat(messenger.getCard().getName()).isEqualTo("Moonrise Intruder");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new VillageMessenger());
        Permanent messenger = findPermanent(player1, "Village Messenger");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(messenger.isTransformed()).isTrue();
        assertThat(messenger.getCard().getName()).isEqualTo("Moonrise Intruder");
    }
}
