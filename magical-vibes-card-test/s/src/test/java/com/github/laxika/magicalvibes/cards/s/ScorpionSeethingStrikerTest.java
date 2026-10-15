package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ScorpionSeethingStriker.class, Forest.class, GallantCitizen.class})
class ScorpionSeethingStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when no creature died this turn")
    void doesNotTriggerWithoutCreatureDeath() {
        addCreatureReady(player1, new ScorpionSeethingStriker());
        addCreatureReady(player1, new GallantCitizen());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature that died this turn lets a creature you control connive")
    void connivesTargetedCreatureAfterCreatureDies() {
        addCreatureReady(player1, new ScorpionSeethingStriker());
        Permanent target = addCreatureReady(player1, new GallantCitizen());
        Permanent opponentCreature = addCreatureReady(player2, new GallantCitizen());

        Card discard = new GallantCitizen();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(discard));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discard));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    void discardingLandDoesNotAddCounterAndScorpionCanTargetItself() {
        Permanent scorpion = addCreatureReady(player1, new ScorpionSeethingStriker());
        Card land = new Forest();
        Card drawn = new GallantCitizen();
        harness.setHand(player1, List.of(land));
        harness.setLibrary(player1, List.of(drawn));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, scorpion.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(land));

        assertThat(scorpion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStepEvenAfterCreatureDeath() {
        addCreatureReady(player1, new ScorpionSeethingStriker());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void removedTargetPreventsDrawAndDiscard() {
        addCreatureReady(player1, new ScorpionSeethingStriker());
        Permanent target = addCreatureReady(player1, new GallantCitizen());
        Card handCard = new Forest();
        Card libraryCard = new GallantCitizen();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillResolvesAfterScorpionLeavesBattlefield() {
        Permanent scorpion = addCreatureReady(player1, new ScorpionSeethingStriker());
        Permanent target = addCreatureReady(player1, new GallantCitizen());
        Card discard = new GallantCitizen();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(discard));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(scorpion);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discard));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
