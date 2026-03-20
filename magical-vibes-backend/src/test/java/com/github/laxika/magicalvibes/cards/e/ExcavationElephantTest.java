package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcavationElephantTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 3/5, no ETB trigger")
    void castWithoutKickerNoTrigger() {
        harness.setHand(player1, List.of(new ExcavationElephant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Excavation Elephant"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker — no graveyard interaction even with artifact in graveyard")
    void castWithoutKickerIgnoresGraveyard() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        harness.setHand(player1, List.of(new ExcavationElephant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castKicked();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Excavation Elephant"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — returns artifact from graveyard to hand")
    void castWithKickerReturnsArtifact() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Cast with kicker — does not offer non-artifact cards from graveyard")
    void castWithKickerDoesNotOfferNonArtifact() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Cast with kicker — empty graveyard, no graveyard choice")
    void castWithKickerEmptyGraveyard() {
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    // ===== Helpers =====

    private void castKicked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExcavationElephant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
    }
}
