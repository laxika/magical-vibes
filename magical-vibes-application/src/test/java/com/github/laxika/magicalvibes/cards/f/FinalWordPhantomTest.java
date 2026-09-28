package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SerumVisions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalWordPhantom.class, SerumVisions.class})
class FinalWordPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("May grant flash during each opponent's end step")
    void grantsFlashDuringOpponentEndStep() {
        addPhantomAndSpell();
        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleMayAbilityChosen(player1, true));
        assertThat(gd.playersWithFlashUntilEndOfTurn).contains(player1.getId());
        addSpellMana();

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Declining the flash permission leaves sorceries uncastable")
    void decliningDoesNotGrantFlash() {
        addPhantomAndSpell();
        advanceToEndStep(player2);

        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleMayAbilityChosen(player1, false));
        addSpellMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not trigger during its controller's end step")
    void doesNotTriggerDuringOwnEndStep() {
        harness.addToBattlefield(player1, new FinalWordPhantom());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addPhantomAndSpell() {
        harness.addToBattlefield(player1, new FinalWordPhantom());
        harness.setHand(player1, List.of(new SerumVisions()));
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
