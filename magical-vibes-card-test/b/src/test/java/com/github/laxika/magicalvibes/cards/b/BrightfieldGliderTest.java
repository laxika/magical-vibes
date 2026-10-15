package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AutarchMammoth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightfieldGlider.class, AutarchMammoth.class})
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

    @Test
    void saddleTapsOtherCreatureAndEnablesAttackBonus() {
        Permanent glider = addCreatureReady(player1, new BrightfieldGlider());
        Permanent saddler = harness.addToBattlefieldAndReturn(player1, new AutarchMammoth());
        saddler.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(saddler.isTapped()).isTrue();
        assertThat(glider.isSaddled()).isTrue();
        assertThat(glider.isTapped()).isFalse();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(glider.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(glider.isSaddled()).isFalse();
        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    void saddledAttackTriggerDoesNotRecheckSaddledOnResolution() {
        Permanent glider = addCreatureReady(player1, new BrightfieldGlider());
        glider.setSaddled(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        glider.setSaddled(false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, glider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glider)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();
    }
}
