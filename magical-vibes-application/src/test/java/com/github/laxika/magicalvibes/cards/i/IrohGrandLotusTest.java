package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.f.FireNationAttacks;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonestWork;
import com.github.laxika.magicalvibes.cards.s.SeismicSense;
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

@CardUsed({IrohGrandLotus.class, FirebendingLesson.class, FireNationAttacks.class,
        GrizzlyBears.class, HonestWork.class, SeismicSense.class, Shock.class})
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

    @Test
    @DisplayName("Iroh's flashback cost is available even when the card already has flashback")
    void canUseGrantedCostInsteadOfPrintedFlashbackCost() {
        addIroh();
        FireNationAttacks spell = new FireNationAttacks();
        harness.setGraveyard(player1, List.of(spell));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Iroh stops granting flashback when it loses its abilities")
    void losingAbilitiesRemovesFlashbackGrant() {
        Permanent iroh = addIroh();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HonestWork()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castEnchantment(player2, 0, iroh.getId());
        resolveAllTriggers();

        prepareMainPhase();
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lesson flashback is unavailable during an opponent's turn")
    void lessonsDoNotGainFlashbackDuringOpponentsTurn() {
        addIroh();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new FirebendingLesson()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Iroh grants flashback only to its controller's graveyard")
    void opponentsGraveyardDoesNotGainFlashback() {
        addIroh();
        prepareMainPhase();
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sorcery Lessons can be flashed back for generic mana")
    void sorceryLessonUsesGenericFlashbackCost() {
        addIroh();
        SeismicSense spell = new SeismicSense();
        harness.setGraveyard(player1, List.of(spell));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flashback does not let a sorcery Lesson be cast during combat")
    void sorceryLessonRetainsSorceryTiming() {
        addIroh();
        harness.setGraveyard(player1, List.of(new SeismicSense()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
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
