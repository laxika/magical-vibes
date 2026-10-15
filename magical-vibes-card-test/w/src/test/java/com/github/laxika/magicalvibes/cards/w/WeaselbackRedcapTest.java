package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaselbackRedcap.class})
class WeaselbackRedcapTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent redcap = addReadyRedcap(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(redcap.getPowerModifier()).isEqualTo(2);
        assertThat(redcap.getToughnessModifier()).isZero();
    }

    @Test
    void repeatedActivationsStack() {
        Permanent redcap = addReadyRedcap(player1);
        addAbilityMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(redcap.getPowerModifier()).isEqualTo(4);
        assertThat(redcap.getToughnessModifier()).isZero();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent redcap = addReadyRedcap(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(redcap.getPowerModifier()).isZero();
        assertThat(redcap.getToughnessModifier()).isZero();
    }

    private Permanent addReadyRedcap(Player player) {
        return addCreatureReady(player, new WeaselbackRedcap());
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent redcap = harness.addToBattlefieldAndReturn(player1, new WeaselbackRedcap());
        redcap.setSummoningSick(true);
        redcap.tap();
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(redcap.getPowerModifier()).isEqualTo(2);
        assertThat(redcap.getToughnessModifier()).isZero();
        assertThat(redcap.isTapped()).isTrue();
    }

    @Test
    void boostsOnlyItsSourceAndOnlyOnResolution() {
        Permanent redcap = addReadyRedcap(player1);
        Permanent anotherRedcap = addReadyRedcap(player1);
        Permanent opposingRedcap = addReadyRedcap(player2);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(redcap.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(redcap.getPowerModifier()).isEqualTo(2);
        assertThat(redcap.getToughnessModifier()).isZero();
        assertThat(anotherRedcap.getPowerModifier()).isZero();
        assertThat(opposingRedcap.getPowerModifier()).isZero();
    }

    private void addAbilityMana(Player player) {
        addAbilityMana(player, 1);
    }

    private void addAbilityMana(Player player, int activations) {
        harness.addMana(player, ManaColor.COLORLESS, activations);
        harness.addMana(player, ManaColor.RED, activations);
    }
}
