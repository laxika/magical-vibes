package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HazyHomunculus;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TroublesomeSpirit.class, WintermoonMesa.class, HazyHomunculus.class})
class TroublesomeSpiritTest extends BaseCardTest {

    private void advanceToEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Taps all lands you control at the beginning of your end step")
    void tapsControlledLands() {
        harness.addToBattlefield(player1, new TroublesomeSpirit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new HazyHomunculus());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());

        advanceToEndStepTrigger();

        assertThat(land.isTapped()).isTrue();
        assertThat(secondLand.isTapped()).isTrue();
        assertThat(nonland.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new TroublesomeSpirit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Lands are tapped on resolution, including lands added after the trigger")
    void tapsLandsPresentAtResolution() {
        harness.addToBattlefield(player1, new TroublesomeSpirit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(land.isTapped()).isFalse();
        Permanent newLand = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());

        resolveAllTriggers();

        assertThat(land.isTapped()).isTrue();
        assertThat(newLand.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still taps lands after Troublesome Spirit leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new TroublesomeSpirit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(spirit);
        gd.playerGraveyards.get(player1.getId()).add(spirit.getCard());

        resolveAllTriggers();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The end-step ability triggers and resolves even with no lands")
    void triggersWithoutLands() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new TroublesomeSpirit());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(spirit.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
    }
}
