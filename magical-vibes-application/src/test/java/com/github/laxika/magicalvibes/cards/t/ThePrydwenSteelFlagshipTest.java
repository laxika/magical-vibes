package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlintHawkIdol;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePrydwenSteelFlagship.class, GlintHawkIdol.class, GrizzlyBears.class})
class ThePrydwenSteelFlagshipTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken artifact entering creates an enhanced Human Knight")
    void createsEnhancedHumanKnightForNontokenArtifact() {
        harness.addToBattlefield(player1, new ThePrydwenSteelFlagship());
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Knight")).isEmpty();
    }
}
