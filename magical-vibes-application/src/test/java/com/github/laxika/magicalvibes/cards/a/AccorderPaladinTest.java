package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccorderPaladin.class, LeoninSkyhunter.class, GoForTheThroat.class})
class AccorderPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Accorder Paladin pushes battle cry trigger onto stack")
    void attackTriggerPushesOntoStack() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Accorder Paladin");
        assertThat(entry.getSourcePermanentId()).isEqualTo(paladin.getId());
    }

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);

        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Both creatures attack
        gs.declareAttackers(gd, player1, List.of(0, 1));
        // Resolve battle cry trigger
        harness.passBothPriorities();

        // Leonin Skyhunter should get +1/+0 from battle cry
        assertThat(skyhunter.getPowerModifier()).isEqualTo(1);
        assertThat(skyhunter.getToughnessModifier()).isEqualTo(0);
        assertThat(skyhunter.getEffectivePower()).isEqualTo(3); // 2 base + 1 battle cry
        assertThat(skyhunter.getEffectiveToughness()).isEqualTo(2); // unchanged
    }

    @Test
    @DisplayName("Battle cry does not boost Accorder Paladin itself")
    void battleCryDoesNotBoostSelf() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);

        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        // Accorder Paladin should NOT get its own battle cry boost
        assertThat(paladin.getPowerModifier()).isEqualTo(0);
        assertThat(paladin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost non-attacking creatures")
    void battleCryDoesNotBoostNonAttackers() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);

        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Only paladin attacks, skyhunter stays back
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Skyhunter should NOT get battle cry boost (not attacking)
        assertThat(skyhunter.getPowerModifier()).isEqualTo(0);
        assertThat(skyhunter.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost opponent's nonattacking creatures")
    void battleCryDoesNotBoostOpponentCreatures() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);

        Permanent oppSkyhunter = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        oppSkyhunter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Opponent's creature should NOT be affected
        assertThat(oppSkyhunter.getPowerModifier()).isEqualTo(0);
        assertThat(oppSkyhunter.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Two Accorder Paladins each give +1/+0 to the other and other attackers")
    void multipleBattleCrySourcesStack() {
        Permanent paladin1 = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin1.setSummoningSick(false);

        Permanent paladin2 = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin2.setSummoningSick(false);

        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // All three attack
        gs.declareAttackers(gd, player1, List.of(0, 1, 2));
        // Resolve both battle cry triggers
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Leonin Skyhunter gets +1/+0 from each paladin = +2/+0
        assertThat(skyhunter.getPowerModifier()).isEqualTo(2);
        assertThat(skyhunter.getToughnessModifier()).isEqualTo(0);
        assertThat(skyhunter.getEffectivePower()).isEqualTo(4); // 2 base + 2

        // Each paladin gets +1/+0 from the OTHER paladin only
        assertThat(paladin1.getPowerModifier()).isEqualTo(1);
        assertThat(paladin2.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Battle cry boost resets at cleanup step")
    void boostResetsAtCleanup() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);

        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        // Verify boost is applied
        assertThat(skyhunter.getPowerModifier()).isEqualTo(1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Boost should be gone
        assertThat(skyhunter.getPowerModifier()).isEqualTo(0);
        assertThat(skyhunter.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Battle cry still resolves after its source is destroyed")
    void battleCryResolvesAfterSourceIsDestroyed() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);
        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1));

        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, paladin.getId());
        harness.assertInGraveyard(player1, "Accorder Paladin");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(skyhunter.getPowerModifier()).isEqualTo(1);
        assertThat(skyhunter.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Battle cry affects only creatures attacking when it resolves")
    void creatureRemovedFromCombatBeforeResolutionIsNotBoosted() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new AccorderPaladin());
        paladin.setSummoningSick(false);
        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1));
        skyhunter.setAttacking(false);
        harness.passBothPriorities();

        assertThat(skyhunter.getPowerModifier()).isZero();
        assertThat(skyhunter.getToughnessModifier()).isZero();
    }
}
