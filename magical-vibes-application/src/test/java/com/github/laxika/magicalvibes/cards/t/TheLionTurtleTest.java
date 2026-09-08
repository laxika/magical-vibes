package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLionTurtle.class, AirbendingLesson.class, GrizzlyBears.class})
class TheLionTurtleTest extends BaseCardTest {

    @Test
    void entersAndGainsThreeLife() {
        harness.setLife(player1, 17);

        harness.enterBattlefieldAndReturn(player1, new TheLionTurtle());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void canAttackWithThreeLessonCardsInGraveyard() {
        harness.setLife(player2, 20);
        setLessonCount(3);
        addCreatureReady(player1, new TheLionTurtle());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void cannotAttackWithFewerThanThreeLessonCardsInGraveyard() {
        setLessonCount(2);
        addCreatureReady(player1, new TheLionTurtle());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockWithThreeLessonCardsInGraveyard() {
        setLessonCount(3);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new TheLionTurtle());

        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void cannotBlockWithFewerThanThreeLessonCardsInGraveyard() {
        setLessonCount(2);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new TheLionTurtle());

        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappingAddsOneChosenColorOfMana() {
        Permanent lionTurtle = addCreatureReady(player1, new TheLionTurtle());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(lionTurtle.isTapped()).isTrue();
    }

    private void setLessonCount(int count) {
        List<Card> lessons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lessons.add(new AirbendingLesson());
        }
        harness.setGraveyard(player1, lessons);
    }
}
