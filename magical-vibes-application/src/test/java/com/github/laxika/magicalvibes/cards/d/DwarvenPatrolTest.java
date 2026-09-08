package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenPatrol.class, GrizzlyBears.class, RagingGoblin.class})
class DwarvenPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent patrol = addPatrol(true);

        advanceToNextTurn(player2);

        assertThat(patrol.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps when its controller casts a nonred spell")
    void untapsWhenControllerCastsNonredSpell() {
        Permanent patrol = addPatrol(true);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap when its controller casts a red spell")
    void doesNotUntapWhenControllerCastsRedSpell() {
        Permanent patrol = addPatrol(true);
        harness.setHand(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(patrol.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a nonred spell")
    void opponentCastingNonredSpellDoesNotUntapIt() {
        Permanent patrol = addPatrol(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(patrol.isTapped()).isTrue();
    }

    private Permanent addPatrol(boolean tapped) {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new DwarvenPatrol());
        if (tapped) {
            patrol.tap();
        }
        return patrol;
    }

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
