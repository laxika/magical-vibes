package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentOfThePass.class, AirbendingLesson.class, Shock.class, GrizzlyBears.class, Island.class})
class SerpentOfThePassTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each noncreature, nonland card in the graveyard")
    void reducesCostForNoncreatureNonlandGraveyardCards() {
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new GrizzlyBears(), new Island()));
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can be cast at instant speed with three Lesson cards in the graveyard")
    void threeLessonsGrantFlashTiming() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast at instant speed with fewer than three Lesson cards")
    void fewerThanThreeLessonsDoNotGrantFlashTiming() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson(), new Shock()));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
