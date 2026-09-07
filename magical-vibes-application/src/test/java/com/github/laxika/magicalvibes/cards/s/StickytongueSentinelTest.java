package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StickytongueSentinel.class, GrizzlyBears.class, Island.class})
class StickytongueSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can return another permanent you control")
    void etbReturnsAnotherPermanentYouControl() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new StickytongueSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StickytongueSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Stickytongue Sentinel");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB rejects a permanent controlled by an opponent as a target")
    void etbRejectsOpponentPermanentTarget() {
        UUID opponentPermanentId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new StickytongueSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentPermanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent you control");
    }

    @Test
    @DisplayName("ETB may be cast without choosing a target")
    void etbMayBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new StickytongueSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Stickytongue Sentinel");
    }
}
