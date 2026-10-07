package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempleBell.class})
class TempleBellTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts draw effect on the stack")
    void activatingPutsOnStack() {
        Permanent bell = addBellReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(bell.getCard());
    }

    @Test
    @DisplayName("Activating ability taps Temple Bell")
    void activatingTapsBell() {
        Permanent bell = addBellReady(player1);

        assertThat(bell.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(bell.isTapped()).isTrue();
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability causes each player to draw a card")
    void resolvingCausesEachPlayerToDraw() {
        addBellReady(player1);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(p2HandBefore + 1);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate twice because it requires tap")
    void cannotActivateTwice() {
        addBellReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Temple Bell stays on battlefield =====

    @Test
    @DisplayName("Temple Bell remains on battlefield after resolution")
    void remainsOnBattlefieldAfterResolution() {
        addBellReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Temple Bell");
    }

    // ===== Helper methods =====

    private Permanent addBellReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TempleBell());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Players draw only when the ability resolves")
    void drawingWaitsForResolution() {
        addBellReady(player1);
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("A newly controlled noncreature Bell can activate on the opponent's turn")
    void newlyControlledBellCanActivateOnOpponentsTurn() {
        Permanent bell = harness.addToBattlefieldAndReturn(player1, new TempleBell());
        bell.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bell.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("The ability still resolves after Temple Bell leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent bell = addBellReady(player1);
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(bell);
        gd.playerGraveyards.get(player1.getId()).add(bell.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Temple Bell");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("Both players lose together when both libraries are empty")
    void emptyLibrariesEndInDraw() {
        addBellReady(player1);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
