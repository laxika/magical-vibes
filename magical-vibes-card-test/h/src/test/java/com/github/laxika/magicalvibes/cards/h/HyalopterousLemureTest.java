package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HyalopterousLemure.class})
class HyalopterousLemureTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives -1/-0 and flying until end of turn")
    void boostsAndGrantsFlying() {
        Permanent lemure = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemure)).isEqualTo(3); // 4 - 1
        assertThat(gqs.getEffectiveToughness(gd, lemure)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Multiple activations stack the power reduction")
    void multipleActivationsStack() {
        Permanent lemure = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemure)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lemure)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent lemure = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());
        lemure.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lemure.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, lemure)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent lemure = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemure)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lemure)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The free ability changes only its source and only when it resolves")
    void affectsOnlySourceOnResolution() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(source.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Repeated free activations can reduce power below zero without killing the creature")
    void repeatedActivationsAllowNegativePower() {
        Permanent lemure = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, lemure)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, lemure)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lemure);
    }
}
