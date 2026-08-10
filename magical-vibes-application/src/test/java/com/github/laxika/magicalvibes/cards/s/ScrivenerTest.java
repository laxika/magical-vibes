package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrivenerTest extends BaseCardTest {

    /**
     * Casts Scrivener and resolves it onto the battlefield, then accepts the may ability.
     */
    private void castAndAcceptMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Scrivener()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Resolving Scrivener triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.setGraveyard(player1, List.of(new Opt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Scrivener()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining may ability does not return anything")
    void decliningMaySkipsAbility() {
        harness.setGraveyard(player1, List.of(new Opt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Scrivener()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Opt");
    }

    @Test
    @DisplayName("Returns an instant card from graveyard to hand")
    void returnsInstantFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new Opt()));
        castAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Opt");
        harness.assertNotInGraveyard(player1, "Opt");
    }

    @Test
    @DisplayName("Chooses a specific instant when multiple cards are in the graveyard")
    void choosesSpecificInstant() {
        harness.setGraveyard(player1, List.of(new Ponder(), new Opt()));
        castAndAcceptMay();

        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Opt");
        harness.assertInGraveyard(player1, "Ponder");
    }

    @Test
    @DisplayName("Cannot return a non-instant card")
    void cannotReturnNonInstant() {
        harness.setGraveyard(player1, List.of(new Ponder(), new Opt()));
        castAndAcceptMay();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("No effect if graveyard has no instant cards")
    void noEffectWithNoInstantsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Ponder()));
        castAndAcceptMay();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(s -> s.contains("no instant card"));
    }

    @Test
    @DisplayName("Player can decline graveyard choice")
    void playerCanDeclineGraveyardChoice() {
        harness.setGraveyard(player1, List.of(new Opt()));
        castAndAcceptMay();

        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Opt");
        harness.assertNotInHand(player1, "Opt");
    }
}
