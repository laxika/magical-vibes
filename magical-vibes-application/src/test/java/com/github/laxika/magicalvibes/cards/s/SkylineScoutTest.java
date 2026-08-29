package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkylineScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} gives Skyline Scout flying until end of turn")
    void payingManaGrantsFlying() {
        Permanent scout = addReadyScout();
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, scout, Keyword.FLYING)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Declining the payment does not give Skyline Scout flying")
    void decliningPaymentDoesNotGrantFlying() {
        Permanent scout = addReadyScout();
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, scout, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying granted by the attack trigger ends at end of turn")
    void flyingEndsAtEndOfTurn() {
        Permanent scout = addReadyScout();
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, scout, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyScout() {
        return addCreatureReady(player1, new SkylineScout());
    }
}
