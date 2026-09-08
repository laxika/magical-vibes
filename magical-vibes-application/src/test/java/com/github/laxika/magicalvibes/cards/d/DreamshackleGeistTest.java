package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamshackleGeist.class, GrizzlyBears.class, Forest.class})
class DreamshackleGeistTest extends BaseCardTest {

    private static final String TAP_MODE = "Tap target creature.";
    private static final String LOCK_MODE = "Target creature doesn't untap during its controller's next untap step.";

    @Test
    @DisplayName("The tap mode taps the chosen creature")
    void tapsChosenCreature() {
        Permanent geist = addCreatureReady(player1, new DreamshackleGeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleListChoice(player1, TAP_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();
        assertThat(geist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The lock mode prevents the chosen creature's next untap")
    void locksChosenCreature() {
        addCreatureReady(player1, new DreamshackleGeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleListChoice(player1, LOCK_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller may choose no mode")
    void mayChooseNoMode() {
        addCreatureReady(player1, new DreamshackleGeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleListChoice(player1, ChooseOneEffect.NO_MODE_LABEL);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("The mode target must be a creature")
    void onlyCreaturesAreLegalTargets() {
        addCreatureReady(player1, new DreamshackleGeist());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToCombat(player1);
        harness.handleListChoice(player1, TAP_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
