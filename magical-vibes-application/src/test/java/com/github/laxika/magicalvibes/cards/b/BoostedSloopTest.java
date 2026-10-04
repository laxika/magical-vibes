package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuidelightOptimizer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoostedSloop.class, GrizzlyBears.class, GuidelightOptimizer.class, Forest.class})
class BoostedSloopTest extends BaseCardTest {

    @Test
    void attacksDrawsThenDiscards() {
        attackWithSloop(List.of(new GrizzlyBears()), new Forest());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void crewAnimatesVehicleAndResetsAtEndOfTurn() {
        Permanent sloop = addCreatureReady(player1, new BoostedSloop());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sloop)).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sloop)).isFalse();
    }

    @Test
    void uncrewedSloopTriggersWhenAnotherCreatureAttacks() {
        Permanent sloop = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        addCreatureReady(player1, new GuidelightOptimizer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GuidelightOptimizer(), new Forest()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sloop)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Guidelight Optimizer");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void multipleOtherAttackersTriggerOnlyOneLoot() {
        harness.addToBattlefield(player1, new BoostedSloop());
        addCreatureReady(player1, new GuidelightOptimizer());
        addCreatureReady(player1, new GuidelightOptimizer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GuidelightOptimizer(), new Forest(), new Forest()));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Guidelight Optimizer");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentAttackingDoesNotTriggerLoot() {
        harness.addToBattlefield(player1, new BoostedSloop());
        addCreatureReady(player2, new GuidelightOptimizer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void summoningSickCreatureCanPayCrewCost() {
        Permanent sloop = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GuidelightOptimizer());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, sloop)).isTrue();
    }

    @Test
    void crewedSloopCannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new BoostedSloop());
        addCreatureReady(player1, new GuidelightOptimizer());
        addCreatureReady(player2, new GuidelightOptimizer());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void emptyHandDiscardsTheCardJustDrawn() {
        attackWithSloop(List.of(), new Forest());

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void crewedSloopCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new BoostedSloop());
        addCreatureReady(player1, new GuidelightOptimizer());
        Permanent first = addCreatureReady(player2, new GuidelightOptimizer());
        Permanent second = addCreatureReady(player2, new GuidelightOptimizer());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void attackWithSloop(List<Card> hand, Card cardToDraw) {
        addCreatureReady(player1, new BoostedSloop());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(cardToDraw));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
    }
}
