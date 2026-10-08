package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidwingHybrid.class, VoltCharge.class})
class VoidwingHybridTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard after proliferating without choosing a permanent")
    void returnsFromGraveyardAfterProliferatingWithoutChoosingAPermanent() {
        var hybrid = new VoidwingHybrid();
        harness.setGraveyard(player1, List.of(hybrid));

        proliferateWithVoltCharge(player1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hybrid);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hybrid);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(hybrid);
        harness.assertInHand(player1, "Voidwing Hybrid");
    }

    @Test
    void opponentProliferatingDoesNotReturnHybrid() {
        var hybrid = new VoidwingHybrid();
        harness.setGraveyard(player1, List.of(hybrid));

        proliferateWithVoltCharge(player2);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hybrid);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hybrid);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferatingReturnsEachCopyFromOwnersGraveyard() {
        var first = new VoidwingHybrid();
        var second = new VoidwingHybrid();
        var opposing = new VoidwingHybrid();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opposing));

        proliferateWithVoltCharge(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposing);
    }

    @Test
    void proliferatingOnBattlefieldDoesNotReturnHybrid() {
        var hybrid = new VoidwingHybrid();
        harness.addToBattlefield(player1, hybrid);

        proliferateWithVoltCharge(player1);

        harness.assertOnBattlefield(player1, "Voidwing Hybrid");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hybrid);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oldTriggerDoesNotReturnCardThatLeftAndReenteredGraveyard() {
        var hybrid = new VoidwingHybrid();
        harness.setGraveyard(player1, List.of(hybrid));
        gd.markGraveyardEntry(hybrid);

        proliferateWithVoltCharge(player1);

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(hybrid));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(hybrid));
        gd.markGraveyardEntry(hybrid);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hybrid);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hybrid);
    }

    @Test
    void combatDamageDealsNormalDamageAndOnePoisonCounter() {
        var attacker = addCreatureReady(player1, new VoidwingHybrid());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosingPlayerForProliferationStillReturnsHybrid() {
        var hybrid = new VoidwingHybrid();
        harness.setGraveyard(player1, List.of(hybrid));
        gd.playerPoisonCounters.put(player2.getId(), 1);

        proliferateWithVoltCharge(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(hybrid);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(hybrid);
    }

    private void proliferateWithVoltCharge(Player player) {
        harness.setHand(player, List.of(new VoltCharge()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player, 0,
                player.equals(player1) ? player2.getId() : player1.getId());
    }
}
