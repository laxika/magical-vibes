package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ReptilDinomorpher.class)
class ReptilDinomorpherTest extends BaseCardTest {

    @Test
    void brontosaurusAbilitySetsStatsTypesAndKeywordsUntilEndOfTurn() {
        Permanent reptil = harness.addToBattlefieldAndReturn(player1, new ReptilDinomorpher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, reptil))
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.HERO);
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, reptil, CardSubtype.DINOSAUR)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void tyrannosaurusRexAbilitySetsStatsTypesAndTrampleUntilEndOfTurn() {
        Permanent reptil = harness.addToBattlefieldAndReturn(player1, new ReptilDinomorpher());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, reptil))
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.HERO);
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, reptil, CardSubtype.DINOSAUR)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isFalse();
    }
}
