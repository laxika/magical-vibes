package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LoporritScout;
import com.github.laxika.magicalvibes.cards.m.MagickedCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SidequestCardCollection.class, MagickedCard.class, LoporritScout.class})
class SidequestCardCollectionTest extends BaseCardTest {

    @Test
    void entersAndDrawsThreeThenDiscardsTwo() {
        Card firstDraw = new LoporritScout();
        Card secondDraw = new LoporritScout();
        Card thirdDraw = new LoporritScout();
        Card keptCard = new LoporritScout();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(new SidequestCardCollection(), keptCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, firstDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondDraw, thirdDraw);
    }

    @Test
    void transformsAtEndStepOnlyWhenEightCardsAreInControllerGraveyard() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, List.of(
                new LoporritScout(), new LoporritScout(), new LoporritScout(), new LoporritScout(),
                new LoporritScout(), new LoporritScout(), new LoporritScout(), new LoporritScout()));

        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(source.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformAtEndStepWithFewerThanEightCardsInControllerGraveyard() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, List.of(
                new LoporritScout(), new LoporritScout(), new LoporritScout(), new LoporritScout(),
                new LoporritScout(), new LoporritScout(), new LoporritScout()));

        beginEndStep(player1);

        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    void magickedCardCanBeCrewed() {
        Permanent source = addTransformedSidequest(player1);
        Permanent creature = addCreatureReady(player1, new LoporritScout());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, source)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerWithSevenCardsEvenIfAnEighthArrivesDuringEndStep() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, graveyardCards(7));

        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1, graveyardCards(8));
        resolveAllTriggers();
        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    void doesNotTransformWhenGraveyardDropsBelowEightBeforeResolution() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, graveyardCards(8));

        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, graveyardCards(7));
        resolveAllTriggers();

        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    void opponentGraveyardDoesNotCountTowardThreshold() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setGraveyard(player2, graveyardCards(8));

        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, graveyardCards(8));

        beginEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    void transformedVehicleDoesNotTransformBackAtNextEndStep() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, graveyardCards(9));

        beginEndStep(player1);
        resolveAllTriggers();
        assertThat(source.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, source)).isFalse();

        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTransformed()).isTrue();
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationEndsAtCleanup() {
        Permanent source = addTransformedSidequest(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LoporritScout());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, source)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, source)).isFalse();
        assertThat(source.isTransformed()).isTrue();
    }

    @Test
    void crewedMagickedCardCannotBeBlockedByGroundCreature() {
        addTransformedSidequest(player1);
        addCreatureReady(player1, new LoporritScout());
        addCreatureReady(player2, new LoporritScout());
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    private List<Card> graveyardCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> (Card) new SidequestCardCollection())
                .toList();
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private Permanent addSidequest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SidequestCardCollection());
    }

    private Permanent addTransformedSidequest(Player player) {
        SidequestCardCollection front = new SidequestCardCollection();
        Permanent source = new Permanent(front);
        source.setCard(front.getBackFaceCard());
        source.setTransformed(true);
        source.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(source);
        return source;
    }
}
