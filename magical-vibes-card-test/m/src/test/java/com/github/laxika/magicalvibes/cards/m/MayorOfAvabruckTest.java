package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MayorOfAvabruck.class, GatstafShepherd.class, DarkthicketWolf.class, AvacynsPilgrim.class})
class MayorOfAvabruckTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Howlpack Alpha when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(mayor.isTransformed()).isTrue();
        assertThat(mayor.getCard().getName()).isEqualTo("Howlpack Alpha");
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(mayor.isTransformed()).isFalse();
        assertThat(mayor.getCard().getName()).isEqualTo("Mayor of Avabruck");
    }

    @Test
    @DisplayName("Howlpack Alpha transforms back when a player cast two or more spells last turn")
    void howlpackTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(mayor.isTransformed()).isFalse();
        assertThat(mayor.getCard().getName()).isEqualTo("Mayor of Avabruck");
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Howlpack Alpha does not transform back when only one spell was cast last turn")
    void howlpackDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(mayor.isTransformed()).isTrue();
        assertThat(mayor.getCard().getName()).isEqualTo("Howlpack Alpha");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(mayor.isTransformed()).isTrue();
        assertThat(mayor.getCard().getName()).isEqualTo("Howlpack Alpha");
    }

    @Test
    @DisplayName("Other Human creatures you control get +1/+1 from front face")
    void frontFaceBoostsOtherHumans() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        // GatstafShepherd is a Human Werewolf — should get the Human boost
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        // Gatstaf Shepherd is 2/2, should be 3/3 with the +1/+1 from Mayor
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mayor does not boost itself (says 'other')")
    void frontFaceDoesNotBoostSelf() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Mayor is 1/1, should remain 1/1 (does not boost itself)
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Human creatures do not get the front face boost")
    void frontFaceDoesNotBoostNonHumans() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        // Darkthicket Wolf is a Wolf, not a Human
        Permanent wolf = addCreatureReady(player1, new DarkthicketWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Humans do not get the front face boost")
    void frontFaceDoesNotBoostOpponentHumans() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent oppShepherd = addCreatureReady(player2, new GatstafShepherd());

        // Opponent's Human should not get the boost
        assertThat(gqs.getEffectivePower(gd, oppShepherd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppShepherd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Back face boosts other Werewolves you control")
    void backFaceBoostsOtherWerewolves() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        // Entering after the upkeep keeps the Shepherd on its front face.
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        // Gatstaf Shepherd (front face) is Human Werewolf — Werewolf qualifies for the boost
        // 2/2 base + 1/1 from Howlpack Alpha = 3/3
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Back face does not boost itself (says 'each other')")
    void backFaceDoesNotBoostSelf() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        // Howlpack Alpha is 3/3, should remain 3/3 (does not boost itself)
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Howlpack Alpha creates a 2/2 Wolf token at controller's end step")
    void createsWolfTokenAtEndStep() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        // Advance to end step (controller's turn)
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        // Should have created a 2/2 green Wolf token
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        Permanent wolfToken = findPermanent(player1, "Wolf");
        assertThat(wolfToken.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wolfToken)).isEqualTo(3);   // 2 base + 1 from Howlpack Alpha
        assertThat(gqs.getEffectiveToughness(gd, wolfToken)).isEqualTo(3); // 2 base + 1 from Howlpack Alpha
    }

    @Test
    @DisplayName("Howlpack Alpha does not create a token on opponent's end step")
    void doesNotCreateTokenOnOpponentEndStep() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        // Advance to opponent's end step
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step — should NOT trigger token creation

        // No Wolf token should be created
        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    @Test
    @DisplayName("A spell cast by the opponent prevents the front-face upkeep trigger")
    void opponentSpellPreventsTransform() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(mayor.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Howlpack Alpha stays transformed after a turn with no spells")
    void backFaceStaysTransformedWhenNoSpellsCast() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(mayor.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Two spells by Howlpack Alpha's controller also cause transformation back")
    void controllerTwoSpellsTransformBack() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(mayor.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(mayor.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforming changes the Human anthem into the Wolf anthem and back")
    void transformationChangesWhichCreaturesReceiveBoost() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent human = addCreatureReady(player1, new AvacynsPilgrim());
        Permanent wolf = addCreatureReady(player1, new DarkthicketWolf());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Howlpack Alpha does not boost opposing Wolves or Werewolves")
    void backFaceDoesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        Permanent wolf = addCreatureReady(player2, new DarkthicketWolf());
        Permanent werewolf = addCreatureReady(player2, new GatstafShepherd());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mayor of Avabruck does not create a Wolf token on its front face")
    void frontFaceDoesNotCreateTokenAtEndStep() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wolf")).isZero();
    }


    @Test
    @DisplayName("The created Wolf is green and returns to 2/2 when Howlpack Alpha transforms back")
    void createdWolfLosesBoostWhenAlphaTransformsBack() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().isToken()).isTrue();
        assertThat(wolf.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

}
