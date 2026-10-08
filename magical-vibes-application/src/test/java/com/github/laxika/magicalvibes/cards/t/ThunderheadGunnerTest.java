package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderheadGunner.class, Forest.class})
class ThunderheadGunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card draws a card")
    void discardDrawsCard() {
        addReadyGunner(player1);
        harness.setHand(player1, List.of(new ThunderheadGunner()));
        harness.setLibrary(player1, List.of(new Forest()));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Thunderhead Gunner");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thunderhead Gunner");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void cannotActivateTwiceInOneTurn() {
        addReadyGunner(player1);
        harness.setHand(player1, List.of(new ThunderheadGunner(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateOutsideMainPhase() {
        addReadyGunner(player1);
        harness.setHand(player1, List.of(new ThunderheadGunner()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithEmptyHand() {
        addReadyGunner(player1);
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during an opponent's main phase")
    void cannotActivateDuringOpponentsTurn() {
        addReadyGunner(player1);
        harness.setHand(player1, List.of(new Forest()));
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a second Gunner while the stack is occupied")
    void cannotActivateWithNonemptyStack() {
        addReadyGunner(player1);
        addReadyGunner(player1);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Summoning sickness and being tapped do not prevent activation")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent gunner = harness.addToBattlefieldAndReturn(player1, new ThunderheadGunner());
        gunner.setSummoningSick(true);
        gunner.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ThunderheadGunner()));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Thunderhead Gunner");
        assertThat(gunner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each Gunner can activate once during the same turn")
    void activationLimitIsPerPermanent() {
        addReadyGunner(player1);
        addReadyGunner(player1);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new ThunderheadGunner(), new ThunderheadGunner()));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Thunderhead Gunner", "Thunderhead Gunner");
    }

    @Test
    @DisplayName("The activation limit resets on a later turn")
    void canActivateAgainOnNextOwnTurn() {
        addReadyGunner(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new ThunderheadGunner()));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyGunner(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ThunderheadGunner());
        perm.setSummoningSick(false);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
