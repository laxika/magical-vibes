package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskanaTheRageMother.class, GrizzlyBears.class, HillGiant.class})
class DuskanaTheRageMotherTest extends BaseCardTest {

    @Test
    @DisplayName("Duskana draws for each own creature with base power and toughness 2/2")
    void drawsForOwnTwoTwoCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        castDuskana();
        resolveStack();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Duskana boosts each attacking own 2/2 creature")
    void boostsAttackingTwoTwoCreatures() {
        addCreatureReady(player1, new DuskanaTheRageMother());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Duskana's attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DuskanaTheRageMother());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    private void castDuskana() {
        harness.setHand(player1, List.of(new DuskanaTheRageMother()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveStack() {
        for (int i = 0; i < 10 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
