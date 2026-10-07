package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Cactuar.class)
class CactuarTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand at its controller's end step if it entered earlier")
    void returnsItselfWhenItDidNotEnterThisTurn() {
        harness.addToBattlefield(player1, new Cactuar());

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cactuar");
        harness.assertInHand(player1, "Cactuar");
    }

    @Test
    @DisplayName("Does not return itself if it entered the battlefield this turn")
    void doesNotReturnWhenItEnteredThisTurn() {
        harness.enterBattlefieldAndReturn(player1, new Cactuar());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cactuar");
        harness.assertNotInHand(player1, "Cactuar");
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new Cactuar());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cactuar");
    }

    @Test
    @DisplayName("Only the copy that entered before this turn returns to hand")
    void tracksEntryTimingSeparatelyForEachCopy() {
        Permanent earlier = harness.addToBattlefieldAndReturn(player1, new Cactuar());
        Permanent fresh = harness.enterBattlefieldAndReturn(player1, new Cactuar());

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fresh).doesNotContain(earlier);
        assertThat(gd.playerHands.get(player1.getId())).contains(earlier.getCard()).doesNotContain(fresh.getCard());
    }

    @Test
    @DisplayName("A controlled Cactuar returns to its owner at its controller's end step")
    void returnsToOwnerInsteadOfController() {
        Cactuar card = new Cactuar();
        card.setOwnerId(player1.getId());
        Permanent cactuar = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(cactuar.getId(), player1.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Control effect", null,
                player2.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT), cactuar.getId(),
                null, null, EffectDuration.PERMANENT, 0));

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cactuar");
        harness.assertInHand(player1, "Cactuar");
        harness.assertNotInHand(player2, "Cactuar");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
