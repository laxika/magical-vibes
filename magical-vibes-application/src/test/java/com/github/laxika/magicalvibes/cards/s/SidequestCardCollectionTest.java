package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagickedCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidequestCardCollection.class, MagickedCard.class, GrizzlyBears.class})
class SidequestCardCollectionTest extends BaseCardTest {

    @Test
    void entersAndDrawsThreeThenDiscardsTwo() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card thirdDraw = new GrizzlyBears();
        Card keptCard = new GrizzlyBears();
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
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(source.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformAtEndStepWithFewerThanEightCardsInControllerGraveyard() {
        Permanent source = addSidequest(player1);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    void magickedCardCanBeCrewed() {
        Permanent source = addTransformedSidequest(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, source)).isTrue();
        assertThat(creature.isTapped()).isTrue();
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
