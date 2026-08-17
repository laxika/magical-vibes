package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrightfieldGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled gives it +1/+2 and flying until end of turn")
    void attacksWhileSaddled() {
        Permanent glider = addCreatureReady(player1, new BrightfieldGlider());
        glider.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger")
    void doesNotTriggerWhenNotSaddled() {
        Permanent glider = addCreatureReady(player1, new BrightfieldGlider());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent glider = addCreatureReady(player1, new BrightfieldGlider());

        declareAttackers(player1, List.of(0));
        glider.setSaddled(true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }
}
