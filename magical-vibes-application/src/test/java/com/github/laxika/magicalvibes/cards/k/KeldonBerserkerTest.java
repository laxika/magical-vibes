package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonBerserker.class, RhysticCave.class})
class KeldonBerserkerTest extends BaseCardTest {

    @Test
    void getsPlusThreePowerWhenAttackingWithNoUntappedLands() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.getToughnessModifier()).isZero();
    }

    @Test
    void doesNotTriggerWhileControllingAnUntappedLand() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());
        harness.addToBattlefield(player1, new RhysticCave());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(berserker.getPowerModifier()).isZero();
    }

    @Test
    void triggersWhenAllControlledLandsAreTapped() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        land.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void doesNotTriggerWhenAnotherControlledLandIsUntapped() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        tappedLand.tap();
        harness.addToBattlefield(player1, new RhysticCave());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(berserker.getPowerModifier()).isZero();
    }

    @Test
    void ignoresUntappedLandsControlledByOpponent() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());
        harness.addToBattlefield(player2, new RhysticCave());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void conditionIsCheckedAgainIfLandBecomesUntappedBeforeResolution() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        land.tap();

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        land.untap();
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isZero();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(berserker.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(berserker.getPowerModifier()).isZero();
        assertThat(berserker.getToughnessModifier()).isZero();
    }
}
