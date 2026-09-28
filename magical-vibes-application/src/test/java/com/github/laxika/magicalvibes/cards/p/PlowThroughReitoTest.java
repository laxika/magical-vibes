package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlowThroughReito.class, GrizzlyBears.class, Plains.class, Forest.class})
class PlowThroughReitoTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen Plains and boosts the target creature by their number")
    void returnsChosenPlainsAndBoostsTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castCard(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(firstPlains.getId(), secondPlains.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstPlains.getId(), secondPlains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest)
                .doesNotContain(firstPlains, secondPlains);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returning no Plains gives no boost and is legal")
    void returningNoPlainsGivesNoBoost() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        castCard(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns only controlled Plains to their owners' hands")
    void returnsOnlyControlledPlainsToTheirOwnersHands() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Plains controlledCardOwnedByOpponent = new Plains();
        controlledCardOwnedByOpponent.setOwnerId(player2.getId());
        Permanent controlledPlains = harness.addToBattlefieldAndReturn(player1, controlledCardOwnedByOpponent);
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player2, List.of());

        castCard(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(controlledPlains.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(controlledPlains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(controlledPlains);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentPlains);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(controlledCardOwnedByOpponent);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("With no controlled Plains, resolves without a choice")
    void resolvesWithoutControlledPlains() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());

        castCard(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentPlains);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        castCard(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(plains.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> castCard(player1, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature");
    }

    private void castCard(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new PlowThroughReito()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0, targetId);
    }

}
