package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UrzasAvenger.class)
class UrzasAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts an activated ability on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new UrzasAvenger());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving gives -1/-1 and grants chosen flying until end of turn")
    void resolvingGrantsMinusAndFlying() {
        Permanent avenger = addCreatureReady(player1, new UrzasAvenger());
        int power = gqs.getEffectivePower(gd, avenger);
        int toughness = gqs.getEffectiveToughness(gd, avenger);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve -> prompts keyword choice
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power - 1);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(toughness - 1);
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.FLYING)).isTrue();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Can choose first strike")
    void canChooseFirstStrike() {
        Permanent avenger = addCreatureReady(player1, new UrzasAvenger());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FIRST_STRIKE");

        assertThat(gqs.hasKeyword(gd, avenger, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Can choose trample")
    void canChooseTrample() {
        Permanent avenger = addCreatureReady(player1, new UrzasAvenger());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "TRAMPLE");

        assertThat(gqs.hasKeyword(gd, avenger, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Both the -1/-1 and the granted keyword wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent avenger = addCreatureReady(player1, new UrzasAvenger());
        int power = gqs.getEffectivePower(gd, avenger);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power - 1);
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power);
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can choose banding")
    void canChooseBanding() {
        Permanent avenger = addCreatureReady(player1, new UrzasAvenger());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BANDING");

        assertThat(gqs.hasKeyword(gd, avenger, Keyword.BANDING)).isTrue();
    }
}
