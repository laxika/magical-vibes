package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KyoshiBattleFan;
import com.github.laxika.magicalvibes.cards.z.ZukosConviction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentOfThePass.class, AirbendingLesson.class, ZukosConviction.class, Island.class,
        KyoshiBattleFan.class})
class SerpentOfThePassTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each noncreature, nonland card in the graveyard")
    void reducesCostForNoncreatureNonlandGraveyardCards() {
        harness.setGraveyard(player1, List.of(
                new ZukosConviction(), new ZukosConviction(), new ZukosConviction(),
                new SerpentOfThePass(), new Island()));
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
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson(), new ZukosConviction()));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Noncreature artifacts in the graveyard also reduce the cost")
    void artifactsReduceCostWithoutGrantingFlash() {
        harness.setGraveyard(player1, List.of(
                new KyoshiBattleFan(), new KyoshiBattleFan(), new KyoshiBattleFan()));
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess reduction removes only generic mana and the creature resolves normally")
    void excessReductionStillRequiresTwoBlueMana() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson(),
                new ZukosConviction(), new ZukosConviction(), new ZukosConviction()));
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Serpent of the Pass");
    }

    @Test
    @DisplayName("Cost reduction cannot replace required blue mana with colorless mana")
    void reductionDoesNotRemoveColoredRequirements() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson(),
                new ZukosConviction(), new ZukosConviction(), new ZukosConviction()));
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Serpent of the Pass");
    }

    @Test
    @DisplayName("Lessons in an opponent's graveyard grant neither flash nor a cost reduction")
    void opponentsGraveyardDoesNotEnableAbilities() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three Lessons allow casting during the opponent's turn")
    void lessonsAllowCastingOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new SerpentOfThePass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
