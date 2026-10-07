package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mosstodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunseedNurturer.class, Mosstodon.class, SkysovereignConsulFlagship.class})
class SunseedNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life at end step when controlling a power-5-or-greater creature and accepting")
    void gainsLifeWhenControllingBigCreature() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        harness.addToBattlefield(player1, new Mosstodon()); // 5/3
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Declining the may ability gains no life")
    void decliningGainsNoLife() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        harness.addToBattlefield(player1, new Mosstodon()); // 5/3
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger without a power-5-or-greater creature")
    void noTriggerWithoutBigCreature() {
        harness.addToBattlefield(player1, new SunseedNurturer()); // 1/1 only
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent's power-5-or-greater creature does not satisfy the intervening-if")
    void opponentBigCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        harness.addToBattlefield(player2, new Mosstodon()); // 5/3
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void noTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        harness.addToBattlefield(player1, new Mosstodon()); // 5/3
        harness.setLife(player1, 20);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Intervening-if: no life if the power-5 creature leaves before resolution")
    void interveningIfFailsAtResolution() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        Permanent big = harness.addToBattlefieldAndReturn(player1, new Mosstodon()); // 5/3
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(big);

        harness.passBothPriorities(); // resolve trigger â€” intervening-if fails

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorlessMana() {
        Permanent nurturer = addCreatureReady(player1, new SunseedNurturer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(nurturer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void uncrewedVehicleDoesNotSatisfyCreatureCondition() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void nurturerCanSatisfyItsOwnConditionWithCounters() {
        Permanent nurturer = harness.addToBattlefieldAndReturn(player1, new SunseedNurturer());
        nurturer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    void powerDroppingBelowFiveBeforeResolutionPreventsLifeGain() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Mosstodon());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        creature.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void triggerStillGainsLifeAfterNurturerLeavesBattlefield() {
        Permanent nurturer = harness.addToBattlefieldAndReturn(player1, new SunseedNurturer());
        harness.addToBattlefield(player1, new Mosstodon());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(nurturer);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    void summoningSickNurturerCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new SunseedNurturer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void tappedNurturerCannotActivateAgain() {
        addCreatureReady(player1, new SunseedNurturer());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
