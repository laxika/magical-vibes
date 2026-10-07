package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlintHawkIdol;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePrydwenSteelFlagship.class, GlintHawkIdol.class, GrizzlyBears.class, SwiftfootBoots.class})
class ThePrydwenSteelFlagshipTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken artifact entering creates an enhanced Human Knight")
    void createsEnhancedHumanKnightForNontokenArtifact() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent knight = findPermanent(player1, "Human Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Human Knight's artifact-entry boost wears off at end of turn")
    void humanKnightBoostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent knight = findPermanent(player1, "Human Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Crew 2 animates The Prydwen")
    void crewAnimatesThePrydwen() {
        Permanent prydwen = harness.addToBattlefieldAndReturn(player1, new ThePrydwenSteelFlagship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(prydwen), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, prydwen)).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A nonartifact creature does not trigger The Prydwen")
    void nonartifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Knight")).isEmpty();
    }

    @Test
    void enteringPrydwenDoesNotTriggerItself() {
        harness.castFromHand(player1, new ThePrydwenSteelFlagship(), "{4}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Prydwen, Steel Flagship");
        assertThat(findPermanents(player1, "Human Knight")).isEmpty();
    }

    @Test
    void opponentsArtifactDoesNotCreateKnight() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SwiftfootBoots(), "{2}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Swiftfoot Boots");
        assertThat(findPermanents(player1, "Human Knight")).isEmpty();
        assertThat(findPermanents(player2, "Human Knight")).isEmpty();
    }

    @Test
    void repeatedArtifactEntriesCreateSeparateKnightsWithoutStackingBonus() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new SwiftfootBoots(), "{2}");
            harness.passBothPriorities();
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Human Knight")).hasSize(2).allSatisfy(knight -> {
            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
        });
    }

    @Test
    void existingKnightRegainsBonusFromArtifactTokenButNoNewKnightIsCreated() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        harness.castFromHand(player1, new SwiftfootBoots(), "{2}");
        harness.passBothPriorities();
        resolveAllTriggers();
        Permanent knight = findPermanent(player1, "Human Knight");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);

        SwiftfootBoots tokenCopy = new SwiftfootBoots();
        tokenCopy.setToken(true);
        harness.enterBattlefieldAndReturn(player1, tokenCopy);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Knight")).containsExactly(knight);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    void opponentsArtifactDoesNotEnhanceExistingKnightOnLaterTurn() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        harness.castFromHand(player1, new SwiftfootBoots(), "{2}");
        harness.passBothPriorities();
        resolveAllTriggers();
        Permanent knight = findPermanent(player1, "Human Knight");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SwiftfootBoots(), "{2}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Knight")).containsExactly(knight);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationEndsWithTurn() {
        Permanent prydwen = harness.addToBattlefieldAndReturn(player1, new ThePrydwenSteelFlagship());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(prydwen), null, null);
        assertThat(bears.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, prydwen)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, prydwen)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, prydwen)).isFalse();
    }
}
