package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YoseiTheMorningStarTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger taps the chosen permanents and the target player skips their next untap step")
    void diesTapsChosenPermanentsAndSkipsUntapStep() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent bears = addReady(player2, new GrizzlyBears());
        Permanent forest = addReady(player2, new Forest());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), forest.getId()));

        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();

        endTurn(); // player 1's turn
        endTurn(); // player 2's turn — their untap step is skipped

        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only one untap step is skipped — the following one untaps normally")
    void skipsOnlyTheNextUntapStep() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent bears = addReady(player2, new GrizzlyBears());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        endTurn();
        endTurn(); // player 2's turn — untap step skipped
        assertThat(bears.isTapped()).isTrue();

        endTurn();
        endTurn(); // player 2's next turn — untaps normally
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing no permanents is legal, but the untap step is still skipped")
    void choosingNoPermanentsStillSkipsUntapStep() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent bears = addReady(player2, new GrizzlyBears());
        bears.tap();

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        endTurn();
        endTurn(); // player 2's turn — untap step still skipped

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The skip lands on the targeted player's untap-step queue and on no other queue")
    void queuesNothingButTheTargetPlayersUntapStepSkip() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        addReady(player2, new GrizzlyBears());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.skipNextUntapStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
        assertThat(gd.skipNextTurnCount).isEmpty();
        assertThat(gd.skipNextDrawStepCount).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount).isEmpty();
    }

    @Test
    @DisplayName("Only the targeted player's permanents may be chosen, up to five of them")
    void onlyTargetPlayersPermanentsAreValidChoices() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent ownBears = addReady(player1, new GrizzlyBears());
        Permanent enemyBears = addReady(player2, new GrizzlyBears());
        Permanent enemyForest = addReady(player2, new Forest());

        killYosei();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds())
                .contains(enemyBears.getId(), enemyForest.getId())
                .doesNotContain(ownBears.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller may target themselves")
    void mayTargetSelf() {
        harness.addToBattlefield(player1, new YoseiTheMorningStar());
        Permanent ownBears = addReady(player1, new GrizzlyBears());

        killYosei();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));

        assertThat(ownBears.isTapped()).isTrue();
    }

    private void killYosei() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID yoseiId = harness.getPermanentId(player1, "Yosei, the Morning Star");
        harness.castInstant(player2, 0, yoseiId);
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    /** Ends the current turn; the other player becomes active and takes their untap step. */
    private void endTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
