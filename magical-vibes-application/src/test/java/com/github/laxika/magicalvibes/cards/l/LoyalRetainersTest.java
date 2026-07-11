package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuanYuSaintedWarrior;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoyalRetainersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        addReadyRetainers(player1);
        harness.setGraveyard(player1, List.of(new GuanYuSaintedWarrior()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Loyal Retainers"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Loyal Retainers"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Returns a legendary creature card from the graveyard to the battlefield")
    void returnsLegendaryCreatureToBattlefield() {
        addReadyRetainers(player1);
        harness.setGraveyard(player1, List.of(new GuanYuSaintedWarrior()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Guan Yu, Sainted Warrior"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Guan Yu, Sainted Warrior"));
    }

    @Test
    @DisplayName("A non-legendary creature in the graveyard is not a valid choice")
    void cannotChooseNonLegendaryCreature() {
        addReadyRetainers(player1);
        // Grizzly Bears (non-legendary) at index 0, Guan Yu (legendary) at index 1.
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GuanYuSaintedWarrior()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyRetainers(player1);
        harness.setGraveyard(player1, List.of(new GuanYuSaintedWarrior()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot activate after attackers are declared")
    void cannotActivateAfterAttackersDeclared() {
        addReadyRetainers(player1);
        harness.setGraveyard(player1, List.of(new GuanYuSaintedWarrior()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    private Permanent addReadyRetainers(Player player) {
        LoyalRetainers card = new LoyalRetainers();
        Permanent retainers = new Permanent(card);
        retainers.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(retainers);
        return retainers;
    }
}
