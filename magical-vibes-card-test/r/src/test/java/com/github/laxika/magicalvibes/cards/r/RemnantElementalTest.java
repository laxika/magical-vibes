package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RemnantElemental.class, Forest.class})
class RemnantElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Remnant Elemental +2/+0 until end of turn")
    void landfallBoostsUntilEndOfTurn() {
        Permanent remnant = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        int initialPower = gqs.getEffectivePower(gd, remnant);
        int initialToughness = gqs.getEffectiveToughness(gd, remnant);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, remnant)).isEqualTo(initialToughness);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, remnant)).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent remnant = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        int initialPower = gqs.getEffectivePower(gd, remnant);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("Land entries without being played stack boosts only after resolution")
    void multipleLandEntriesStackBoosts() {
        Permanent remnant = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        int initialPower = gqs.getEffectivePower(gd, remnant);
        int initialToughness = gqs.getEffectiveToughness(gd, remnant);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(initialPower);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(initialPower + 2);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(initialPower + 4);
        assertThat(gqs.getEffectiveToughness(gd, remnant)).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("Each Remnant Elemental receives only its own landfall boost")
    void multipleElementalsEachBoostThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        int initialPower = gqs.getEffectivePower(gd, first);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(initialPower + 2);
    }
}
