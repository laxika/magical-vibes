package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlewiseValor.class, GrizzlyBears.class, FountainOfYouth.class})
class BattlewiseValorTest extends BaseCardTest {

    @Test
    @DisplayName("Battlewise Valor boosts the target and lets its controller scry 1")
    void boostsAndScries() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BattlewiseValor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Battlewise Valor");
    }

    @Test
    @DisplayName("Battlewise Valor's boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BattlewiseValor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Battlewise Valor cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new BattlewiseValor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom when targeting an opponent's creature")
    void scriesOwnLibraryAndBoostsOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BattlewiseValor top = new BattlewiseValor();
        FountainOfYouth next = new FountainOfYouth();
        GrizzlyBears opponentTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new BattlewiseValor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        harness.assertInGraveyard(player1, "Battlewise Valor");
    }

    @Test
    @DisplayName("An empty library does not stop the boost or spell resolution")
    void resolvesWithEmptyLibrary() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BattlewiseValor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Battlewise Valor");
    }

    @Test
    @DisplayName("No scry happens if the only target leaves before resolution")
    void doesNotScryWhenTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FountainOfYouth top = new FountainOfYouth();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new BattlewiseValor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerGraveyards.get(player1.getId()).add(bear.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Battlewise Valor");
    }
}
