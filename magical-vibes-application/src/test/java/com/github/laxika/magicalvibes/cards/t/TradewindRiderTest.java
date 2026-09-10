package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TradewindRider.class, HornedTurtle.class, Forest.class})
class TradewindRiderTest extends BaseCardTest {

    private static final int BOUNCE_ABILITY = 0;

    @Test
    @DisplayName("Returns target creature to its owner's hand, tapping the source and two other creatures")
    void bouncesTargetCreature() {
        Permanent rider = addReadyRider(player1);
        Permanent helper1 = addReadyCreature(player1);
        Permanent helper2 = addReadyCreature(player1);
        prepareMainPhase();

        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());

        harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Horned Turtle");
        harness.assertInHand(player2, "Horned Turtle");
        assertThat(rider.isTapped()).isTrue();
        assertThat(helper1.isTapped()).isTrue();
        assertThat(helper2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can return a noncreature permanent such as a land")
    void bouncesLand() {
        Permanent rider = addReadyRider(player1);
        addReadyCreature(player1);
        addReadyCreature(player1);
        prepareMainPhase();

        Permanent victim = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Can return the source permanent to its owner's hand")
    void bouncesSource() {
        Permanent rider = addReadyRider(player1);
        addReadyCreature(player1);
        addReadyCreature(player1);
        prepareMainPhase();

        harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, rider.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tradewind Rider");
        harness.assertInHand(player1, "Tradewind Rider");
    }

    @Test
    @DisplayName("Cannot activate without two other untapped creatures")
    void requiresTwoOtherUntappedCreatures() {
        Permanent rider = addReadyRider(player1);
        addReadyCreature(player1); // only one other creature besides the Rider
        prepareMainPhase();

        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two untapped creatures controlled by the activating player")
    void requiresTwoUntappedCreaturesYouControl() {
        Permanent rider = addReadyRider(player1);
        Permanent readyHelper = addReadyCreature(player1);
        Permanent tappedHelper = addReadyCreature(player1);
        tappedHelper.tap();
        addReadyCreature(player2);
        prepareMainPhase();

        Permanent victim = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rider.isTapped()).isFalse();
        assertThat(readyHelper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source creature has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new TradewindRider());
        addReadyCreature(player1);
        addReadyCreature(player1);
        prepareMainPhase();

        Permanent victim = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyRider(Player player) {
        return addCreatureReady(player, new TradewindRider());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new HornedTurtle());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
