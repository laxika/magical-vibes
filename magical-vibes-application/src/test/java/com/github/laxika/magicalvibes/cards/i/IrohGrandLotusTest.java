package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IrohGrandLotus.class, FirebendingLesson.class, GrizzlyBears.class, Shock.class})
class IrohGrandLotusTest extends BaseCardTest {

    @Test
    @DisplayName("Firebending adds mana that lasts through combat")
    void firebendingAddsManaUntilEndOfCombat() {
        addIroh();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("During your turn, non-Lesson instants and sorceries gain flashback for their mana cost")
    void grantsFlashbackToNonLessonInstantsAndSorceries() {
        addIroh();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Lesson cards gain flashback for one generic mana")
    void grantsLessonsFixedFlashbackCost() {
        addIroh();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new FirebendingLesson()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Iroh does not grant flashback to non-Lesson permanents")
    void doesNotGrantFlashbackToNonLessonPermanents() {
        addIroh();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Iroh's graveyard flashback grants work only during its controller's turn")
    void flashbackGrantsOnlyWorkDuringControllersTurn() {
        addIroh();
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addIroh() {
        return addCreatureReady(player1, new IrohGrandLotus());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
