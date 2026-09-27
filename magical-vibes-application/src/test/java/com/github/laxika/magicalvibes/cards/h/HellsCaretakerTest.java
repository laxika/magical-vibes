package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.j.Juxtapose;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellsCaretaker.class, DurkwoodBoars.class, HeadlessHorseman.class, Juxtapose.class})
class HellsCaretakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature during upkeep returns target creature card from graveyard to the battlefield")
    void reanimatesTargetCreatureDuringUpkeep() {
        Permanent caretaker = addCreatureReady(player1, new HellsCaretaker());
        Permanent fodder = addCreatureReady(player1, new DurkwoodBoars());

        Card target = new HeadlessHorseman();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(caretaker.isTapped()).isTrue();
        // Sacrificed creature is in the graveyard
        harness.assertInGraveyard(player1, "Durkwood Boars");
        // Reanimated creature is on the battlefield, no longer in the graveyard
        harness.assertOnBattlefield(player1, "Headless Horseman");
        harness.assertNotInGraveyard(player1, "Headless Horseman");
    }

    @Test
    @DisplayName("Cannot activate outside the controller's upkeep")
    void cannotActivateOutsideUpkeep() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new DurkwoodBoars());

        Card target = new HeadlessHorseman();
        harness.setGraveyard(player1, List.of(target));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new DurkwoodBoars());

        Card target = new Juxtapose();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new DurkwoodBoars());

        Card target = new HeadlessHorseman();
        harness.setGraveyard(player1, List.of(target));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new DurkwoodBoars());

        Card target = new HeadlessHorseman();
        harness.setGraveyard(player2, List.of(target));

        advanceToUpkeep(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
