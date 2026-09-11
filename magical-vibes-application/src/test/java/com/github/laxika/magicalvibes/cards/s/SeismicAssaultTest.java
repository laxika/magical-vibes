package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.w.WallOfNets;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicAssault.class, CityOfTraitors.class, RagingGoblin.class, Spellbook.class, WallOfNets.class})
class SeismicAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability starts discard-cost choice before stack entry")
    void activationStartsDiscardChoice() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new RagingGoblin(), new CityOfTraitors()));

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a land pays cost and puts ability on stack")
    void choosingLandPaysCostAndStacksAbility() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new RagingGoblin(), new Spellbook(), new CityOfTraitors()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 2);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "City of Traitors");
        harness.assertInGraveyard(player1, "City of Traitors");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot activate without a land card in hand")
    void cannotActivateWithoutLandCard() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a land card");
    }

    @Test
    @DisplayName("Cannot choose nonland for discard cost")
    void cannotChooseNonLandForDiscardCost() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new RagingGoblin(), new CityOfTraitors()));

        harness.activateAbility(player1, 0, null, player2.getId());

        // Choosing an invalid index re-prompts instead of throwing
        harness.handleCardChosen(player1, 0);

        // State should still be awaiting discard cost choice (re-prompted)
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Illegal target is rejected before discard cost is paid")
    void illegalTargetRejectedBeforePayingCost() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new CityOfTraitors()));
        UUID illegalTarget = UUID.randomUUID();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, illegalTarget))
                .isInstanceOf(IllegalStateException.class);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new CityOfTraitors()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature")
    void deals2DamageToCreature() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new CityOfTraitors()));
        harness.addToBattlefield(player2, new RagingGoblin());

        UUID goblinId = harness.getPermanentId(player2, "Raging Goblin");
        harness.activateAbility(player1, 0, null, goblinId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Deals exactly 2 damage to a surviving target creature")
    void dealsExactly2DamageToSurvivingCreature() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new CityOfTraitors()));
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfNets());

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Wall of Nets");
    }

    private void addReadySeismicAssault(Player player) {
        harness.addToBattlefield(player, new SeismicAssault());
    }
}

