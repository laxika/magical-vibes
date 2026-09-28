package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellchainScatter.class, Shock.class})
class SpellchainScatterTest extends BaseCardTest {

    @Test
    void unKickedSpellConjuresDuplicateAndDiscardsItAtNextEndStep() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Shock") && card.isTokenCard())
                .findFirst().orElseThrow();
        assertThat(duplicate.isTokenCard()).isTrue();

        resolveStack();
        assertThat(gd.playerHands.get(player1.getId())).contains(duplicate);

        advanceToNextEndStep();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate);
    }

    @Test
    void kickedSpellKeepsTheConjuredDuplicate() {
        harness.setHand(player1, List.of(new SpellchainScatter(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Shock") && card.isTokenCard())
                .findFirst().orElseThrow();
        resolveStack();
        advanceToNextEndStep();

        assertThat(gd.playerHands.get(player1.getId())).contains(duplicate);
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
