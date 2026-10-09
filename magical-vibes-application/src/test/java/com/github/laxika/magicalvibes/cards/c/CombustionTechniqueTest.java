package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombustionTechnique.class, AirbendingLesson.class, HillGiant.class})
class CombustionTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage and marks the target for exile instead of dying")
    void dealsBaseDamageAndMarksTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Deals one additional damage for each Lesson in the caster's graveyard")
    void countsLessonsInCasterGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        cast(target);

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    @Test
    void ignoresOpponentsLessonsAndOwnNonLessons() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.setGraveyard(player2, List.of(new AirbendingLesson(), new CombustionTechnique()));

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Combustion Technique");
    }

    @Test
    void countsEveryLessonIncludingOtherCopiesOfThisSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new CombustionTechnique(), new CombustionTechnique()));

        cast(target);

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    @Test
    void countsLessonsAtResolutionRatherThanAtCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new CombustionTechnique()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));

        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    @Test
    void exilesSurvivingTargetIfSacrificedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(target);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, target));

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    @Test
    void exileReplacementExpiresAfterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(target);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, target));

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new CombustionTechnique()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
