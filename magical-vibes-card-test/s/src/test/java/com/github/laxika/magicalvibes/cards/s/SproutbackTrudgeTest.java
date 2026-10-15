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

@CardUsed({SproutbackTrudge.class})
class SproutbackTrudgeTest extends BaseCardTest {

    @Test
    void reducesGenericCostByLifeGainedThisTurn() {
        gainLife(player1, 3);
        harness.setHand(player1, List.of(new SproutbackTrudge()));
        addManaForReducedCost();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sproutback Trudge");
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
        gainLife(player1, 1);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.END_STEP, this::acceptCastOffer);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
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

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trudge);
    }

    @Test
    void cannotCastFromGraveyardWithoutPayingReducedCost() {
        SproutbackTrudge trudge = new SproutbackTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        gainLife(player1, 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            acceptCastOffer();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trudge);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(trudge.getId()));
        harness.assertNotOnBattlefield(player1, "Sproutback Trudge");
    }

    @Test
    void mayDeclineGraveyardCast() {
        SproutbackTrudge trudge = new SproutbackTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        gainLife(player1, 3);

        advanceToEndStep(player1);
        addManaForReducedCost();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trudge);
        harness.assertNotOnBattlefield(player1, "Sproutback Trudge");
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        SproutbackTrudge trudge = new SproutbackTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        gainLife(player1, 3);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trudge);
    }

    @Test
    void opponentsLifeGainDoesNotEnableGraveyardTrigger() {
        SproutbackTrudge trudge = new SproutbackTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        gainLife(player2, 3);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trudge);
    }

    @Test
    void gainingLifeAfterEndStepBeginsDoesNotTrigger() {
        SproutbackTrudge trudge = new SproutbackTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        advanceToEndStep(player1);

        gainLife(player1, 3);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trudge);
    }

    @Test
    void reductionCountsAllLifeGainedDespiteLifeLost() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "life loss"));
        gainLife(player1, 2);
        gainLife(player1, 2);
        harness.setHand(player1, List.of(new SproutbackTrudge()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sproutback Trudge");
    }

    @Test
    void excessLifeGainDoesNotReduceColoredManaCost() {
        gainLife(player1, 20);
        harness.setHand(player1, List.of(new SproutbackTrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sproutback Trudge");
    }

    private void gainLife(Player player, int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player.getId(), amount));
    }

    private void addManaForReducedCost() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void acceptCastOffer() {
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }
}
