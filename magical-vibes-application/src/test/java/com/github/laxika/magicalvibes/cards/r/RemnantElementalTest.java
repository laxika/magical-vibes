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
        int initialPower = remnant.getEffectivePower();
        int initialToughness = remnant.getEffectiveToughness();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(remnant.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(remnant.getEffectiveToughness()).isEqualTo(initialToughness);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(remnant.getEffectivePower()).isEqualTo(initialPower);
        assertThat(remnant.getEffectiveToughness()).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent remnant = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        int initialPower = remnant.getEffectivePower();
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(remnant.getEffectivePower()).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("Land entries without being played stack boosts only after resolution")
    void multipleLandEntriesStackBoosts() {
        Permanent remnant = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        int initialPower = remnant.getEffectivePower();
        int initialToughness = remnant.getEffectiveToughness();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(remnant.getEffectivePower()).isEqualTo(initialPower);
        harness.passBothPriorities();
        assertThat(remnant.getEffectivePower()).isEqualTo(initialPower + 2);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(remnant.getEffectivePower()).isEqualTo(initialPower + 4);
        assertThat(remnant.getEffectiveToughness()).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("Each Remnant Elemental receives only its own landfall boost")
    void multipleElementalsEachBoostThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        int initialPower = first.getEffectivePower();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(second.getEffectivePower()).isEqualTo(initialPower + 2);
    }
}
