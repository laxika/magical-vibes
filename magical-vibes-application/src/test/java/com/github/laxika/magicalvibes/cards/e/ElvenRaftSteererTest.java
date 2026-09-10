package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenRaftSteerer.class, Forest.class, GrizzlyBears.class})
class ElvenRaftSteererTest extends BaseCardTest {

    private static final String TAP = "Tap target creature an opponent controls.";
    private static final String UNTAP = "Untap target creature you control.";

    @Test
    void landfallTapsTargetCreatureAnOpponentControls() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        playLand();
        harness.handleListChoice(player1, TAP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void landfallUntapsTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();

        playLand();
        harness.handleListChoice(player1, UNTAP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void tapModeCannotTargetYourCreature() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        playLand();
        harness.handleListChoice(player1, TAP);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapModeCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        playLand();
        harness.handleListChoice(player1, UNTAP);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void playLand() {
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
    }
}
