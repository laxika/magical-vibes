package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class KruinOutlawTest extends BaseCardTest {

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Terror of Kruin Pass when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
        assertThat(gqs.getEffectivePower(gd, outlaw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outlaw)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(outlaw.isTransformed()).isFalse();
        assertThat(outlaw.getCard().getName()).isEqualTo("Kruin Outlaw");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Terror of Kruin Pass transforms back when a player cast two or more spells last turn")
    void terrorTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(outlaw.isTransformed()).isFalse();
        assertThat(outlaw.getCard().getName()).isEqualTo("Kruin Outlaw");
        assertThat(gqs.getEffectivePower(gd, outlaw)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, outlaw)).isEqualTo(2);
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not transform back when only one spell was cast last turn")
    void terrorDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Only 1 spell cast last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
    }

    // ===== Static menace: Werewolves you control have menace (back face) =====

    @Test
    @DisplayName("Terror of Kruin Pass grants menace to other werewolves you control")
    void backFaceGrantsMenaceToOtherWerewolves() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add another werewolf
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Terror of Kruin Pass itself has menace")
    void backFaceHasMenaceItself() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");
        outlaw.setSummoningSick(false);

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not grant menace to non-werewolf creatures")
    void backFaceDoesNotGrantMenaceToNonWerewolves() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add a non-werewolf creature
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not grant menace to opponent's werewolves")
    void backFaceDoesNotGrantMenaceToOpponentWerewolves() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add a werewolf to opponent's battlefield
        Permanent oppShepherd = addCreatureReady(player2, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, oppShepherd, Keyword.MENACE)).isFalse();
    }

    // ===== Front face does not grant menace =====

    @Test
    @DisplayName("Front face Kruin Outlaw does not grant menace to werewolves")
    void frontFaceDoesNotGrantMenace() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isFalse();
    }

}
