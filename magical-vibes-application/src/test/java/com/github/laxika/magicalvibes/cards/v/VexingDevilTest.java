package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VexingDevilTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.setHand(player1, List.of(new VexingDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // creature resolves → ETB trigger stacks
        harness.passBothPriorities(); // ETB resolves → opponent is offered the choice
    }

    @Test
    @DisplayName("Opponent declines — Devil stays, no damage")
    void decliningKeepsDevil() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vexing Devil"));
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Vexing Devil");
    }

    @Test
    @DisplayName("Opponent accepts — takes 4 damage and Devil is sacrificed")
    void acceptingDamagesAndSacrifices() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vexing Devil"));
        harness.assertInGraveyard(player1, "Vexing Devil");
    }
}
