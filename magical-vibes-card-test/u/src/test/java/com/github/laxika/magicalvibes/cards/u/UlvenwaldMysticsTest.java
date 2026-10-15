package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlvenwaldMystics.class})
class UlvenwaldMysticsTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Ulvenwald Primordials when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(mystics.isTransformed()).isTrue();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Primordials");
        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(mystics.isTransformed()).isFalse();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Mystics");
    }

    @Test
    @DisplayName("Ulvenwald Primordials transforms back when a player cast two or more spells last turn")
    void primordialTransformsBackWhenTwoSpellsCast() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());

        // Transform to Ulvenwald Primordials first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve transform
        assertThat(mystics.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(mystics.isTransformed()).isFalse();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Mystics");
        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ulvenwald Primordials does not transform back when only one spell was cast last turn")
    void primordialDoesNotTransformWhenOneSpellCast() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());

        // Transform to Ulvenwald Primordials first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(mystics.isTransformed()).isTrue();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Primordials");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(mystics.isTransformed()).isTrue();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Primordials");
    }

    @Test
    @DisplayName("Ulvenwald Primordials can activate regeneration ability")
    void primordialCanActivateRegeneration() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());

        // Transform to Ulvenwald Primordials
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();

        // Activate regeneration ability
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities(); // resolve

        assertThat(mystics.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Ulvenwald Primordials from lethal damage")
    void regenerationSavesPrimordialFromLethalDamage() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());

        // Transform to Ulvenwald Primordials (5/5)
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();

        // Give it a regeneration shield and set up as blocker
        mystics.setRegenerationShield(1);
        mystics.setSummoningSick(false);
        mystics.setBlocking(true);
        mystics.addBlockingTarget(0);

        // Create a 6/3 attacker to deal lethal damage.
        Permanent attacker = addCreatureReady(player2, new UlvenwaldMystics());
        attacker.setAttacking(true);
        attacker.setPowerModifier(3); // 3+3=6 power

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Ulvenwald Primordials should survive via regeneration
        harness.assertOnBattlefield(player1, "Ulvenwald Primordials");
        assertThat(mystics.isTapped()).isTrue();
        assertThat(mystics.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's spell prevents the front face from transforming")
    void opponentSpellPreventsTransform() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(mystics.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The back face stays transformed after a spell-free turn")
    void primordialStaysTransformedWhenNoSpellsCast() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(mystics.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("A tapped Primordials can create multiple shields without regenerating immediately")
    void tappedPrimordialCanCreateMultipleShields() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new UlvenwaldMystics());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();
        mystics.tap();
        mystics.setMarkedDamage(2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mystics.getRegenerationShield()).isEqualTo(2);
        assertThat(mystics.getMarkedDamage()).isEqualTo(2);
        assertThat(mystics.isTapped()).isTrue();
    }

}
