package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.StewardOfValeron;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveskimmerAven.class, StewardOfValeron.class})
class WaveskimmerAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new WaveskimmerAven());
        Permanent steward = addCreatureReady(player1, new StewardOfValeron());

        declareAttackers(player1, List.of(1)); // Steward of Valeron attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Aven attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent aven = addCreatureReady(player1, new WaveskimmerAven());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new WaveskimmerAven());
        Permanent steward = addCreatureReady(player1, new StewardOfValeron());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new WaveskimmerAven());
        Permanent steward = addCreatureReady(player1, new StewardOfValeron());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Waveskimmer Aven"));
        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Aven contributes an exalted boost to the lone attacker")
    void multipleExaltedSourcesStack() {
        addCreatureReady(player1, new WaveskimmerAven());
        addCreatureReady(player1, new WaveskimmerAven());
        Permanent steward = addCreatureReady(player1, new StewardOfValeron());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's lone attacker receives no exalted boost from your Aven")
    void opponentAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new WaveskimmerAven());
        Permanent steward = addCreatureReady(player2, new StewardOfValeron());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(2);
    }
}
