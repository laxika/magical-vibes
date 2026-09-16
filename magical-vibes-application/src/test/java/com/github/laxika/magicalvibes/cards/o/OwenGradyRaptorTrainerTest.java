package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RegalImperiosaur;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OwenGradyRaptorTrainer.class, RegalImperiosaur.class, GrizzlyBears.class})
class OwenGradyRaptorTrainerTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({
            "Put a reach counter on it, REACH, REACH",
            "Put a menace counter on it, MENACE, MENACE",
            "Put a trample counter on it, TRAMPLE, TRAMPLE",
            "Put a haste counter on it, HASTE, HASTE"
    })
    @DisplayName("Puts the chosen keyword counter on the target Dinosaur")
    void putsChosenCounterOnTargetDinosaur(String mode, CounterType counterType, Keyword keyword) {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new RegalImperiosaur());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, dinosaur.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);

        assertThat(dinosaur.getCounterCount(counterType)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dinosaur, keyword)).isTrue();
    }

    @Test
    @DisplayName("Can target only a Dinosaur")
    void cannotTargetNonDinosaur() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dinosaur");
    }
}
