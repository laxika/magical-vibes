package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TradeRoutes.class, Forest.class, GrizzlyBears.class, Island.class})
class TradeRoutesTest extends BaseCardTest {

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
        assertThat(gameLogContains("fizzles")).isTrue();
    }

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
        assertThat(gameLogContains("draws a card")).isTrue();
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

    @Test
    @DisplayName("Bounce ability cannot target an opponent's land")
    void bounceCannotTargetOpponentsLand() {
        harness.addToBattlefield(player1, new TradeRoutes());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land you control");
    }
    @Test
    @DisplayName("Discard is paid before the draw resolves")
    void discardIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new TradeRoutes());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Discard-draw ability requires mana as well as a land")
    void discardDrawRequiresMana() {
        harness.addToBattlefield(player1, new TradeRoutes());
        harness.setHand(player1, List.of(new Island()));

        assertThatThrownBy(() -> {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleCardChosen(player1, 0);
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Island");
        harness.assertNotInGraveyard(player1, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bounce returns a controlled land to its owner rather than its controller")
    void bounceReturnsLandToItsOwner() {
        harness.addToBattlefield(player1, new TradeRoutes());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerBattlefields.get(player1.getId()).add(land);
        gd.stolenCreatures.put(land.getId(), player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Bounce can be activated repeatedly without tapping Trade Routes")
    void bounceCanBeActivatedRepeatedly() {
        harness.addToBattlefield(player1, new TradeRoutes());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, island.getId());
        harness.activateAbility(player1, 0, 0, null, forest.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
