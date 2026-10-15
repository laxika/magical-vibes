package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightOfTheMeek.class, GrizzlyBears.class, ManifoldMouse.class, FountainOfYouth.class})
class MightOfTheMeekTest extends BaseCardTest {

    @Test
    void grantsTrampleDrawsCardAndBoostsWithMouse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ManifoldMouse());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNotBoostWithoutMouseAndTemporaryEffectsExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void targetedMouseEnablesBoostAndBothEffectsExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new MightOfTheMeek()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Might of the Meek");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsMouseIsLegalTargetButDoesNotEnableBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManifoldMouse());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new MightOfTheMeek()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Might of the Meek");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void mouseEnteringBeforeResolutionEnablesBoostOnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManifoldMouse());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new MightOfTheMeek()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new ManifoldMouse());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Might of the Meek");
    }

    @Test
    void mouseLeavingBeforeResolutionPreventsBoostButNotDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManifoldMouse());
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new MightOfTheMeek()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mouse);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Might of the Meek");
    }

    @Test
    void doesNotDrawWhenOnlyTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManifoldMouse());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new MightOfTheMeek()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Might of the Meek");
    }
}
