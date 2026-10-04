package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SerumVisions;
import com.github.laxika.magicalvibes.model.ManaColor;
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
    @DisplayName("Casting permission is available immediately during an opponent's end step")
    void grantsFlashDuringOpponentEndStep() {
        addPhantomAndSpell();
        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        addSpellMana();

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting permission is unavailable during an opponent's main phase")
    void doesNotGrantFlashOutsideEndStep() {
        addPhantomAndSpell();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        addSpellMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not trigger during its controller's end step")
    void doesNotTriggerDuringOwnEndStep() {
        addPhantomAndSpell();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        addSpellMana();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A Phantom entering during an opponent's end step immediately permits sorceries")
    void enteringDuringEndStepGrantsPermission() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.castFromHand(player1, new FinalWordPhantom(), "{2}{U}");
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        harness.setHand(player1, List.of(new SerumVisions()));
        addSpellMana();

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting permission stops when Phantom leaves the battlefield")
    void permissionRequiresPhantomToRemainOnBattlefield() {
        addPhantomAndSpell();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        addSpellMana();
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new SerumVisions()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
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
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
