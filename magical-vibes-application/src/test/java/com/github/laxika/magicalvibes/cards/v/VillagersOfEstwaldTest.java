package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VillagersOfEstwaldTest extends BaseCardTest {

    // ===== Werewolf transform: front -> back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Howlpack of Estwald when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(villagers.isTransformed()).isTrue();
        assertThat(villagers.getCard().getName()).isEqualTo("Howlpack of Estwald");
        assertThat(gqs.getEffectivePower(gd, villagers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, villagers)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(villagers.isTransformed()).isFalse();
        assertThat(villagers.getCard().getName()).isEqualTo("Villagers of Estwald");
    }

    // ===== Werewolf transform: back -> front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Howlpack of Estwald transforms back when a player cast two or more spells last turn")
    void howlpackTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        // Transform to Howlpack of Estwald first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(villagers.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(villagers.isTransformed()).isFalse();
        assertThat(villagers.getCard().getName()).isEqualTo("Villagers of Estwald");
        assertThat(gqs.getEffectivePower(gd, villagers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, villagers)).isEqualTo(3);
    }

    @Test
    @DisplayName("Howlpack of Estwald does not transform back when only one spell was cast last turn")
    void howlpackDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        // Transform to Howlpack of Estwald first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(villagers.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(villagers.isTransformed()).isTrue();
        assertThat(villagers.getCard().getName()).isEqualTo("Howlpack of Estwald");
    }

    // ===== Transform triggers on every upkeep =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new VillagersOfEstwald());
        Permanent villagers = findPermanent(player1, "Villagers of Estwald");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(villagers.isTransformed()).isTrue();
        assertThat(villagers.getCard().getName()).isEqualTo("Howlpack of Estwald");
    }

}
