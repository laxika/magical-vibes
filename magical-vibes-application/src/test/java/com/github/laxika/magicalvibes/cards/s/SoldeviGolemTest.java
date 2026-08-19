package com.github.laxika.magicalvibes.cards.s;

import java.util.List;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoldeviGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger untaps the opponent's creature and the Golem")
    void acceptUntapsBoth() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves both creatures tapped")
    void declineLeavesBothTapped() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Golem does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.tap();

        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped creature an opponent controls is not a legal target")
    void untappedOpponentCreatureIsNotLegal() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent untapped = addCreatureReady(player2, new GrizzlyBears());

        triggerUpkeep(player1);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped creature you control is not a legal target")
    void ownTappedCreatureIsNotLegal() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        own.tap();

        triggerUpkeep(player1);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
    }

    private Permanent addReadyGolem(Player player) {
        Permanent perm = new Permanent(new SoldeviGolem());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
