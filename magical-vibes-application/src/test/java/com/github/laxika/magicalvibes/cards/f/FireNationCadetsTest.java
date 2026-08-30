package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationCadets.class, AirbendingLesson.class})
class FireNationCadetsTest extends BaseCardTest {

    @Test
    @DisplayName("Firebending adds two red mana when a Lesson is in the controller's graveyard")
    void firebendingAddsManaWithLessonInGraveyard() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        Permanent cadets = addReadyCadets();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(cadets.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Firebending does not trigger without a Lesson in the controller's graveyard")
    void firebendingDoesNotAddManaWithoutLesson() {
        addReadyCadets();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The activated ability gives Fire Nation Cadets +1/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent cadets = addReadyCadets();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cadets)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cadets)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cadets)).isEqualTo(1);
    }

    private Permanent addReadyCadets() {
        return addCreatureReady(player1, new FireNationCadets());
    }
}
