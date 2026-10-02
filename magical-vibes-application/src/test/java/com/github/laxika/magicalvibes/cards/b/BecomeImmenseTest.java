package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BecomeImmense.class, GrizzlyBears.class, FountainOfYouth.class})
class BecomeImmenseTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and gives target creature +6/+6")
    void delvesAndBoostsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new BecomeImmense()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = bears.getId();
        harness.castInstantWithMultipleGraveyardExile(player1, 0, targetId, List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(6);
        assertThat(bears.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("The +6/+6 bonus wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BecomeImmense()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = bears.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BecomeImmense()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Delve can pay only part of the generic cost")
    void partialDelveLeavesUnselectedCardsInGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card first = new GrizzlyBears();
        Card unselected = new FountainOfYouth();
        Card last = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, unselected, last));
        harness.setHand(player1, List.of(new BecomeImmense()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, bears.getId(), List.of(0, 2));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, last);
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(6);
        assertThat(bears.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("Delve cannot replace the required green mana")
    void delveCannotPayColoredMana() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> graveyard = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new BecomeImmense()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, bears.getId(), List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Become Immense");
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot exile more cards than the generic cost")
    void cannotDelveMoreThanFiveCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> graveyard = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new BecomeImmense()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, bears.getId(), List.of(0, 1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("generic cost");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Become Immense");
    }

    @Test
    @DisplayName("Can boost a creature controlled by an opponent without delving")
    void canTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card unselected = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(unselected));
        harness.setHand(player1, List.of(new BecomeImmense()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(6);
        assertThat(bears.getToughnessModifier()).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unselected);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Become Immense");
    }
}
