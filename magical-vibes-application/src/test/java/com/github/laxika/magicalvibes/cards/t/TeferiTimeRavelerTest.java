package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferiTimeRaveler.class, Forest.class, GrizzlyBears.class, Shock.class, TamiyosEpiphany.class})
class TeferiTimeRavelerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents can cast spells at sorcery timing but not during combat")
    void restrictsOpponentsToSorceryTiming() {
        harness.addToBattlefield(player1, new TeferiTimeRaveler());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller can still cast an instant during an opponent's turn")
    void doesNotRestrictController() {
        harness.addToBattlefield(player1, new TeferiTimeRaveler());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("+1 lets the controller cast a sorcery at instant timing until their next turn")
    void plusOneGrantsSorceryFlashUntilNextTurn() {
        addReadyTeferi(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TamiyosEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.getGameService().passPriority(gd, player2);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("+1's sorcery flash permission expires when the controller's next turn begins")
    void plusOneExpiresAtNextTurn() {
        addReadyTeferi(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TamiyosEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.getGameService().passPriority(gd, player2);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("-3 returns a target creature and draws a card")
    void minusThreeBouncesCreatureAndDraws() {
        addReadyTeferi(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("-3 may resolve without a target and still draws a card")
    void minusThreeCanResolveWithoutTarget() {
        addReadyTeferi(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("-3 cannot target a land")
    void minusThreeRejectsLandTarget() {
        addReadyTeferi(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTeferi(Player player) {
        Permanent teferi = harness.addToBattlefieldAndReturn(player, new TeferiTimeRaveler());
        teferi.setCounterCount(CounterType.LOYALTY, 4);
        teferi.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return teferi;
    }
}
