package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WarbreakTrumpeter.class)
class WarbreakTrumpeterTest extends BaseCardTest {

    @Test
    void turningFaceUpCreatesTheChosenNumberOfGoblins() {
        harness.setHand(player1, List.of(new WarbreakTrumpeter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent trumpeter = findPermanent(player1, "Warbreak Trumpeter");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(trumpeter));
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        assertThat(trumpeter.isFaceDown()).isFalse();
        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(2).allSatisfy(goblin -> {
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
            assertThat(goblin.getEffectivePower()).isEqualTo(1);
            assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void choosingZeroCreatesNoGoblins() {
        harness.setHand(player1, List.of(new WarbreakTrumpeter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent trumpeter = findPermanent(player1, "Warbreak Trumpeter");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(trumpeter));
        harness.handleXValueChosen(player1, 0);

        assertThat(trumpeter.isFaceDown()).isFalse();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }
}
