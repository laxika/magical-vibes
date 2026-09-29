package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SproutbackTrudge.class)
class SproutbackTrudgeTest extends BaseCardTest {

    @Test
    void reducesGenericCostByLifeGainedThisTurn() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.setHand(player1, List.of(new SproutbackTrudge()));
        addManaForReducedCost();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sproutback Trudge"));
    }

    @Test
    void doesNotReduceCostForOpponentsLifeGain() {
        gd.lifeGainedThisTurn.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new SproutbackTrudge()));
        addManaForReducedCost();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayCastItFromGraveyardAtYourEndStepAfterGainingLife() {
        SproutbackTrudge trudge = new SproutbackTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(trudge);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(trudge.getId()));
    }

    @Test
    void doesNotTriggerFromGraveyardWithoutLifeGain() {
        SproutbackTrudge trudge = new SproutbackTrudge();
        harness.setGraveyard(player1, List.of(trudge));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trudge);
    }

    private void addManaForReducedCost() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
