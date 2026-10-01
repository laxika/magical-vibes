package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsoulProdigy.class, DirgurIslandDragon.class})
class DragonsoulProdigyTest extends BaseCardTest {

    @Test
    void firstOmenSpellConjuresHastyDuplicateUntilNextEndStep() {
        harness.setHand(player1, List.of(new DragonsoulProdigy(), new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();

        Permanent duplicate = findPermanent(player1, "Dirgur Island Dragon");
        assertThat(duplicate.getCard().isToken()).isFalse();
        assertThat(gqs.hasKeyword(gd, duplicate, Keyword.HASTE)).isTrue();
        var duplicateCardId = duplicate.getCard().getId();

        advanceToNextEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(duplicateCardId));
    }

    @Test
    void triggersOnlyOnceForOmenSpellsEachTurn() {
        harness.setHand(player1, List.of(
                new DragonsoulProdigy(), new DirgurIslandDragon(), new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveStack();

        assertThat(countPermanents(player1, "Dirgur Island Dragon")).isEqualTo(1);
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveStack();
    }
}
