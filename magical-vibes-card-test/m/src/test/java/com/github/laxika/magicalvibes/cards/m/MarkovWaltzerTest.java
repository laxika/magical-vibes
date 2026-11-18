package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarkovWaltzer.class, AmbushViper.class})
class MarkovWaltzerTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    @Test
    @DisplayName("Boosts two chosen creatures you control by +1/+0")
    void boostsTwoOwnCreatures() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent viper1 = harness.addToBattlefieldAndReturn(player1, new AmbushViper());
        Permanent viper2 = harness.addToBattlefieldAndReturn(player1, new AmbushViper());

        advanceToCombat(player1);
        chooseTargets(viper1.getId(), viper2.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(PermanentGrants.powerBoostOf(gd, viper1)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, viper1)).isEqualTo(0);
        assertThat(PermanentGrants.powerBoostOf(gd, viper2)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, viper2)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can stop after a single target")
    void boostsSingleTarget() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new AmbushViper());

        advanceToCombat(player1);
        chooseTargets(viper.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, viper)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new AmbushViper());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(enemy.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        harness.addToBattlefield(player1, new AmbushViper());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new AmbushViper());

        advanceToCombat(player1);
        chooseTargets(viper.getId());
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, viper)).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, viper)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can choose zero targets without boosting any creature")
    void canChooseZeroTargets() {
        Permanent waltzer = harness.addToBattlefieldAndReturn(player1, new MarkovWaltzer());
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new AmbushViper());

        advanceToCombat(player1);
        chooseTargets();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, waltzer)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, viper)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself when it is the only creature")
    void canBoostItself() {
        Permanent waltzer = harness.addToBattlefieldAndReturn(player1, new MarkovWaltzer());

        advanceToCombat(player1);
        chooseTargets(waltzer.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, waltzer)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, waltzer)).isZero();
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent waltzer = harness.addToBattlefieldAndReturn(player1, new MarkovWaltzer());
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new AmbushViper());

        advanceToCombat(player1);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(viper.getId(), viper.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(viper.getId(), waltzer.getId()));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, waltzer)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, viper)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still boosts the remaining target when one target leaves the battlefield")
    void resolvesWithOneRemainingTarget() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AmbushViper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AmbushViper());

        advanceToCombat(player1);
        chooseTargets(first.getId(), second.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, first));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The triggered ability resolves after Markov Waltzer leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent waltzer = harness.addToBattlefieldAndReturn(player1, new MarkovWaltzer());
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new AmbushViper());

        advanceToCombat(player1);
        chooseTargets(viper.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, waltzer));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, viper)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, viper)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void chooseTargets(UUID... targetIds) {
        harness.handleMultiplePermanentsChosen(player1, List.of(targetIds));
    }
}
