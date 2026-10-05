package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlatypusBear.class, AirbendingLesson.class})
class PlatypusBearTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, mills two cards from its controller's library")
    void entersAndMillsTwoCards() {
        Card first = new PlatypusBear();
        Card second = new PlatypusBear();
        harness.setHand(player1, List.of(new PlatypusBear()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Cannot attack without a Lesson card in its controller's graveyard")
    void cannotAttackWithoutLesson() {
        Permanent bear = addReadyPlatypusBear();

        assertThatThrownBy(() -> declareAttackers(List.of(battlefieldIndex(bear))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack as though it did not have defender with a Lesson card in its controller's graveyard")
    void canAttackWithLesson() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        Permanent bear = addReadyPlatypusBear();
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(battlefieldIndex(bear)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("A Lesson card in an opponent's graveyard does not enable attacking")
    void opponentLessonDoesNotEnableAttacking() {
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));
        Permanent bear = addReadyPlatypusBear();

        assertThatThrownBy(() -> declareAttackers(List.of(battlefieldIndex(bear))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A non-Lesson card in its controller's graveyard does not enable attacking")
    void nonLessonDoesNotEnableAttacking() {
        harness.setGraveyard(player1, List.of(new PlatypusBear()));
        Permanent bear = addReadyPlatypusBear();

        assertThatThrownBy(() -> declareAttackers(List.of(battlefieldIndex(bear))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Removing the last Lesson before combat disables attacking")
    void cannotAttackAfterLastLessonLeavesGraveyard() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        Permanent bear = addReadyPlatypusBear();
        harness.setGraveyard(player1, List.of());

        assertThatThrownBy(() -> declareAttackers(List.of(battlefieldIndex(bear))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A Lesson milled by the enter trigger enables attacking")
    void milledLessonEnablesAttacking() {
        Card lesson = new AirbendingLesson();
        Card other = new PlatypusBear();
        harness.setHand(player1, List.of(new PlatypusBear()));
        harness.setLibrary(player1, List.of(lesson, other));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lesson, other);
        Permanent bear = findPermanent(player1, "Platypus-Bear");
        bear.setSummoningSick(false);
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(battlefieldIndex(bear)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    private Permanent addReadyPlatypusBear() {
        return addCreatureReady(player1, new PlatypusBear());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
