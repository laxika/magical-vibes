package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StickytongueSentinel.class, Island.class})
class StickytongueSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can return another permanent you control")
    void etbReturnsAnotherPermanentYouControl() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(islandId);

        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Stickytongue Sentinel");
    }

    @Test
    @DisplayName("ETB does not target the source or permanents controlled by an opponent")
    void etbExcludesSourceAndOpponentPermanents() {
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Stickytongue Sentinel");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("ETB rejects a permanent controlled by an opponent as a target")
    void etbRejectsOpponentPermanentTarget() {
        UUID opponentPermanentId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        UUID ownPermanentId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentPermanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, ownPermanentId);
        resolveAllTriggers();
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("ETB may be cast without choosing a target")
    void etbMayBeCastWithoutTarget() {
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Stickytongue Sentinel");
    }

    @Test
    @DisplayName("ETB can decline while another controlled permanent is available")
    void etbCanDeclineWithLegalTarget() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Stickytongue Sentinel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB returns a controlled permanent to its owner rather than its controller")
    void etbReturnsToOpponentOwnersHand() {
        Island island = new Island();
        island.setOwnerId(player2.getId());
        UUID islandId = harness.addToBattlefieldAndReturn(player1, island).getId();
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, islandId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
    }

    @Test
    @DisplayName("ETB does not return a target that an opponent controls before resolution")
    void etbRechecksTargetsController() {
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, island.getId());

        gd.playerBattlefields.get(player1.getId()).remove(island);
        gd.playerBattlefields.get(player2.getId()).add(island);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertNotInHand(player2, "Island");
    }

    @Test
    @DisplayName("ETB still returns the target after the Sentinel leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        harness.castFromHand(player1, new StickytongueSentinel(), "{2}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, islandId);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof StickytongueSentinel);
        resolveAllTriggers();

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
    }
}
