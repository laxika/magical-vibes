package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WildSlash;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HumbleDefector.class, Forest.class, WildSlash.class})
class HumbleDefectorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and gives control to the target opponent")
    void drawsTwoCardsAndGivesControlToTargetOpponent() {
        addReadyDefector();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Humble Defector");
        harness.assertOnBattlefield(player2, "Humble Defector");
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        addReadyDefector();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Cannot be activated during the opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addReadyDefector();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps immediately as a cost and stays tapped after control changes")
    void paysTapCostBeforeResolution() {
        Permanent defector = addReadyDefector();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(defector.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Humble Defector");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(defector.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Humble Defector");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent defector = addReadyDefector();
        defector.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(defector.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws two cards even if the source dies before resolution")
    void drawsWhenSourceDiesInResponse() {
        Permanent defector = addReadyDefector();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new WildSlash()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, defector.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Humble Defector");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Humble Defector");
    }

    @Test
    @DisplayName("Can be activated in response to a spell during its controller's end step")
    void canActivateInResponseDuringEndStep() {
        Permanent defector = addReadyDefector();
        harness.forceStep(TurnStep.END_STEP);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(defector.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Humble Defector");

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Control persists into the opponent's turn and they can give it back")
    void newControllerCanActivateOnTheirNextTurn() {
        addReadyDefector();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Humble Defector");
        int handSizeBeforeActivation = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBeforeActivation + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Humble Defector");
        harness.assertNotOnBattlefield(player2, "Humble Defector");
    }

    private Permanent addReadyDefector() {
        Permanent defector = addCreatureReady(player1, new HumbleDefector());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return defector;
    }
}
