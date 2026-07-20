package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WatchersOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Activating exiles Watchers of the Dead as a cost and puts the ability on the stack")
    void activatingExilesSelfAsCost() {
        addReadyWatchers(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Watchers of the Dead"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Watchers of the Dead"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent keeps two graveyard cards of their choice and exiles the rest")
    void opponentKeepsTwoExilesRest() {
        addReadyWatchers(player1);
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 5 cards, keep 2 -> the opponent chooses 3 to exile.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Opponent with two or fewer graveyard cards keeps them all")
    void opponentWithTwoOrFewerKeepsAll() {
        addReadyWatchers(player1);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not affect the controller's own graveyard")
    void doesNotAffectControllerGraveyard() {
        addReadyWatchers(player1);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    private Permanent addReadyWatchers(Player player) {
        Permanent perm = new Permanent(new WatchersOfTheDead());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
