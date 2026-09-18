package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrainedPronghorn.class, GiantWarthog.class, EmberShot.class})
class TrainedPronghornTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card prevents all combat damage dealt to Trained Pronghorn this turn")
    void discardPreventsAllDamageToSelfThisTurn() {
        Permanent pronghorn = addCreatureReady(player1, new TrainedPronghorn());
        Permanent blocker = addCreatureReady(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new GiantWarthog()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pronghorn);
        assertThat(pronghorn.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Discarding a card prevents noncombat damage dealt to Trained Pronghorn this turn")
    void discardPreventsNoncombatDamageToSelfThisTurn() {
        Permanent pronghorn = addCreatureReady(player1, new TrainedPronghorn());
        harness.setHand(player1, List.of(new GiantWarthog(), new EmberShot()));
        harness.setLibrary(player1, List.of(new GiantWarthog()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 7);
        harness.castInstant(player1, 0, pronghorn.getId());
        harness.passBothPriorities();

        assertThat(pronghorn.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        addCreatureReady(player1, new TrainedPronghorn());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
