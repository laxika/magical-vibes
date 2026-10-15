package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.InkDissolver;
import com.github.laxika.magicalvibes.cards.b.BramblewoodParagon;
import com.github.laxika.magicalvibes.cards.e.EgoErasure;
import com.github.laxika.magicalvibes.cards.t.TaureanMauler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MudbuttonClanger.class, InkDissolver.class, BramblewoodParagon.class,
        TaureanMauler.class, EgoErasure.class})
class MudbuttonClangerTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new MudbuttonClanger())); // Goblin Warrior — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing the shared-type card gives +1/+1")
    void revealBuffsSelf() {
        Permanent clanger = addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new MudbuttonClanger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(clanger.getPowerModifier()).isEqualTo(1);
        assertThat(clanger.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing the shared-type card leaves it on top of the library")
    void revealingLeavesCardOnTop() {
        addCreatureReady(player1, new MudbuttonClanger());
        MudbuttonClanger topCard = new MudbuttonClanger();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The Kinship boost wears off at cleanup")
    void buffWearsOffAtEndOfTurn() {
        Permanent clanger = addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new MudbuttonClanger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(clanger.getPowerModifier()).isZero();
        assertThat(clanger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Declining to reveal leaves the creature unbuffed")
    void decliningDoesNothing() {
        Permanent clanger = addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new MudbuttonClanger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(clanger.getPowerModifier()).isZero();
        assertThat(clanger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new InkDissolver())); // Merfolk Wizard — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new MudbuttonClanger());
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Sharing only Warrior is sufficient for kinship")
    void sharingOnlyWarriorBuffsSelf() {
        Permanent clanger = addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new BramblewoodParagon()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(clanger.getPowerModifier()).isEqualTo(1);
        assertThat(clanger.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A changeling in the library shares a creature type")
    void changelingOnTopBuffsSelf() {
        Permanent clanger = addCreatureReady(player1, new MudbuttonClanger());
        TaureanMauler topCard = new TaureanMauler();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(clanger.getPowerModifier()).isEqualTo(1);
        assertThat(clanger.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Kinship does not trigger during the opponent's upkeep")
    void opponentsUpkeepDoesNotTrigger() {
        Permanent clanger = addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new MudbuttonClanger()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(clanger.getPowerModifier()).isZero();
        assertThat(clanger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Kinship checks current creature types after Ego Erasure resolves")
    void losingCreatureTypesInResponsePreventsReveal() {
        Permanent clanger = addCreatureReady(player1, new MudbuttonClanger());
        harness.setLibrary(player1, List.of(new MudbuttonClanger()));
        harness.setHand(player1, List.of(new EgoErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(clanger.getPowerModifier()).isEqualTo(-2);
        assertThat(clanger.getToughnessModifier()).isZero();
    }
}
