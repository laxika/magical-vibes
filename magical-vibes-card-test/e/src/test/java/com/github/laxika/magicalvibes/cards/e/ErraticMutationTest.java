package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({ErraticMutation.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class,
        Island.class, Shock.class, AirElemental.class})
class ErraticMutationTest extends BaseCardTest {

    @Test
    void boostsTargetByFirstNonlandManaValueAndBottomsAllRevealedCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(shock), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bottomsAllRevealedLandsWhenNoNonlandIsFound() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void usesManaValueOfFirstNonlandCardForBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card forest = new Forest();
        Card erraticMutation = new ErraticMutation();
        harness.setLibrary(player1, List.of(forest, erraticMutation));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(erraticMutation), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(erraticMutation, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNothingWhenLibraryIsEmpty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void stopsAtFirstNonlandAndBottomsRevealedCardsBelowUnrevealedCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card forest = new Forest();
        Card shock = new Shock();
        Card unrevealed = new AirElemental();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, shock, unrevealed, island));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(forest, shock);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, island, shock, forest);
    }

    @Test
    void zeroManaValueNonlandStopsRevealWithoutChangingCreatureStats() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card zeroCost = new FountainOfYouth();
        Card unrevealed = new AirElemental();
        harness.setLibrary(player1, List.of(zeroCost, unrevealed));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, zeroCost);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creatureWithZeroToughnessDiesAfterRevealedCardsAreBottomed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card forest = new Forest();
        Card revealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, revealed));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void revealsNothingWhenTargetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card forest = new Forest();
        Card revealed = new AirElemental();
        harness.setLibrary(player1, List.of(forest, revealed));
        harness.setHand(player1, List.of(new ErraticMutation()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, revealed);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Erratic Mutation");
    }
}
