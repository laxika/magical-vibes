package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TheLordOfTheEagles;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenRaftSteerer.class, Forest.class, TheLordOfTheEagles.class})
class ElvenRaftSteererTest extends BaseCardTest {

    private static final String TAP = "Tap target creature an opponent controls.";
    private static final String UNTAP = "Untap target creature you control.";

    @Test
    void landfallTapsTargetCreatureAnOpponentControls() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheLordOfTheEagles());

        playLand();
        harness.handleListChoice(player1, TAP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void landfallUntapsTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheLordOfTheEagles());
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TheLordOfTheEagles());
        harness.addToBattlefield(player2, new TheLordOfTheEagles());

        playLand();
        harness.handleListChoice(player1, TAP);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapModeCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        harness.addToBattlefield(player1, new TheLordOfTheEagles());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TheLordOfTheEagles());

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

    @Test
    void canUntapItselfWithNoOpponentCreatures() {
        Permanent steerer = harness.addToBattlefieldAndReturn(player1, new ElvenRaftSteerer());
        steerer.tap();

        playLand();
        harness.handleListChoice(player1, UNTAP);
        harness.handlePermanentChosen(player1, steerer.getId());
        harness.passBothPriorities();

        assertThat(steerer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landEnteringWithoutBeingPlayedTriggers() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheLordOfTheEagles());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.handleListChoice(player1, TAP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapModeCanTargetAnAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheLordOfTheEagles());
        target.tap();

        playLand();
        harness.handleListChoice(player1, TAP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untapModeCanTargetAnUntappedCreature() {
        Permanent steerer = harness.addToBattlefieldAndReturn(player1, new ElvenRaftSteerer());

        playLand();
        harness.handleListChoice(player1, UNTAP);
        harness.handlePermanentChosen(player1, steerer.getId());
        harness.passBothPriorities();

        assertThat(steerer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tapModeCannotTargetALand() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        harness.addToBattlefield(player2, new TheLordOfTheEagles());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        playLand();
        harness.handleListChoice(player1, TAP);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapAbilityStillResolvesAfterSourceLeaves() {
        Permanent steerer = harness.addToBattlefieldAndReturn(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheLordOfTheEagles());

        playLand();
        harness.handleListChoice(player1, TAP);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(steerer);
        gd.playerGraveyards.get(player1.getId()).add(steerer.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapAbilityDoesNotTapTargetThatBecomesYourCreature() {
        harness.addToBattlefield(player1, new ElvenRaftSteerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheLordOfTheEagles());

        playLand();
        harness.handleListChoice(player1, TAP);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void playLand() {
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
    }
}
