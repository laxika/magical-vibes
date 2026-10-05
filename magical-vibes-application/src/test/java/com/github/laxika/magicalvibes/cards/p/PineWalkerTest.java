package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CanyonLurkers;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PineWalker.class, CanyonLurkers.class})
class PineWalkerTest extends BaseCardTest {

    @Test
    void turnsFaceUpAndUntapsItself() {
        harness.setHand(player1, List.of(new PineWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent walker = findPermanent(player1, "Pine Walker");
        walker.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(walker));
        harness.passBothPriorities();

        assertThat(walker.isTapped()).isFalse();
    }

    @Test
    void turnsFaceUpAndUntapsAnotherCreatureYouControl() {
        addCreatureReady(player1, new PineWalker());
        harness.setHand(player1, List.of(new CanyonLurkers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lurkers = findPermanent(player1, "Canyon Lurkers");
        lurkers.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lurkers));
        harness.passBothPriorities();

        assertThat(lurkers.isTapped()).isFalse();
    }

    @Test
    void untapsOnlyTheCreatureThatTurnedFaceUp() {
        Permanent walker = addCreatureReady(player1, new PineWalker());
        Permanent other = addCreatureReady(player1, new CanyonLurkers());
        Permanent lurkers = addCreatureReady(player1, new CanyonLurkers());
        lurkers.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        walker.tap();
        other.tap();
        lurkers.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.turnFaceUp(player1, 2);
        resolveAllTriggers();

        assertThat(lurkers.isTapped()).isFalse();
        assertThat(walker.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    void doesNotUntapAnOpponentsCreatureThatTurnsFaceUp() {
        addCreatureReady(player1, new PineWalker());
        Permanent lurkers = addCreatureReady(player2, new CanyonLurkers());
        lurkers.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        lurkers.tap();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.turnFaceUp(player2, 0);
        resolveAllTriggers();

        assertThat(lurkers.isFaceDown()).isFalse();
        assertThat(lurkers.isTapped()).isTrue();
    }

    @Test
    void faceDownWalkerDoesNotUntapAnotherCreature() {
        Permanent walker = addCreatureReady(player1, new PineWalker());
        walker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent lurkers = addCreatureReady(player1, new CanyonLurkers());
        lurkers.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        lurkers.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.turnFaceUp(player1, 1);
        resolveAllTriggers();

        assertThat(lurkers.isFaceDown()).isFalse();
        assertThat(lurkers.isTapped()).isTrue();
    }
}
