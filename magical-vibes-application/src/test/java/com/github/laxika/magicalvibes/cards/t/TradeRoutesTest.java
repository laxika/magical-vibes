package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TradeRoutes.class, Forest.class, GrizzlyBears.class, Island.class})
class TradeRoutesTest extends BaseCardTest {

    // ===== Ability 0: {1}: Return target land you control to its owner's hand =====

    @Test
    @DisplayName("Bounce ability returns own land to hand")
    void bounceReturnsOwnLandToHand() {
        harness.addToBattlefieldAndReturn(player1, new TradeRoutes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Bounce ability cannot be activated without mana")
    void bounceCannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new TradeRoutes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Bounce ability cannot target a nonland permanent")
    void bounceCannotTargetNonlandPermanent() {
        harness.addToBattlefieldAndReturn(player1, new TradeRoutes());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    @DisplayName("Bounce ability fizzles if target land leaves control before resolution")
    void bounceFizzlesIfTargetChangesController() {
        harness.addToBattlefieldAndReturn(player1, new TradeRoutes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, land.getId());

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Ability 1: {1}, Discard a land card: Draw a card =====

    @Test
    @DisplayName("Discard-draw ability only allows land cards to be discarded")
    void discardDrawOnlyLandsValid() {
        harness.addToBattlefieldAndReturn(player1, new TradeRoutes());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Discarding a land pays the cost and drawing resolves")
    void discardLandDrawsACard() {
        harness.addToBattlefieldAndReturn(player1, new TradeRoutes());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Discard-draw ability cannot be activated without a land in hand")
    void discardDrawRequiresLandInHand() {
        harness.addToBattlefieldAndReturn(player1, new TradeRoutes());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

}
