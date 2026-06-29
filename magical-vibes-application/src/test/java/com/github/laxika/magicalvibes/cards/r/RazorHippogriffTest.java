package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.PalladiumMyr;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazorHippogriffTest extends BaseCardTest {

    /**
     * Casts Razor Hippogriff and resolves it onto the battlefield.
     * The mandatory ETB triggered ability is placed on the stack.
     */
    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RazorHippogriff()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Razor Hippogriff puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new RazorHippogriff()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Razor Hippogriff");
    }

    // ===== ETB: return artifact and gain life =====

    @Test
    @DisplayName("ETB returns artifact from graveyard to hand and gains life equal to mana value")
    void returnsArtifactAndGainsLife() {
        // GoldMyr is a 2-mana artifact creature
        harness.setGraveyard(player1, List.of(new GoldMyr()));
        castAndResolve();

        // Resolve ETB → graveyard choice prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        // GoldMyr moved from graveyard to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gold Myr"));

        // Gained 2 life (Gold Myr's mana value)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("ETB gains life based on returned card's mana value")
    void gainsLifeEqualToManaValue() {
        // PalladiumMyr is a 3-mana artifact creature
        harness.setGraveyard(player1, List.of(new PalladiumMyr()));
        castAndResolve();

        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        // Gained 3 life (Palladium Myr's mana value)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Choosing specific artifact when multiple are in graveyard")
    void choosesSpecificArtifactFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GoldMyr(), new PalladiumMyr()));
        castAndResolve();

        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        // Choose PalladiumMyr (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        // PalladiumMyr returned to hand, GoldMyr stays in graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Palladium Myr"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Palladium Myr"));

        // Gained 3 life (Palladium Myr's mana value)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Returning a zero mana value artifact does not gain life")
    void zeroManaValueArtifactGainsNoLife() {
        // Memnite is a 0-mana artifact creature (mana value 0)
        harness.setGraveyard(player1, List.of(new Memnite()));
        castAndResolve();

        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        // Memnite returned to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memnite"));

        // No life gained (mana value is 0, per CR 119.8 gaining 0 life is not a life gain event)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== No artifacts in graveyard =====

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        castAndResolve();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no artifact cards in graveyard"));
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard has only non-artifact cards")
    void noEffectWithOnlyNonArtifactsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndResolve();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no artifact cards in graveyard"));
        // GrizzlyBears stays in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Only artifact cards are valid choices =====

    @Test
    @DisplayName("Cannot choose non-artifact card from graveyard")
    void cannotChooseNonArtifactFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GoldMyr()));
        castAndResolve();

        harness.passBothPriorities();

        // Index 0 is GrizzlyBears (non-artifact creature) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    // ===== Declining =====

    @Test
    @DisplayName("Player can decline graveyard choice and no life is gained")
    void decliningDoesNotGainLife() {
        harness.setGraveyard(player1, List.of(new GoldMyr()));
        castAndResolve();

        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handleGraveyardCardChosen(player1, -1);

        // GoldMyr stays in graveyard, not in hand
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gold Myr"));

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Razor Hippogriff stays on battlefield =====

    @Test
    @DisplayName("Razor Hippogriff remains on battlefield after ETB resolves")
    void remainsOnBattlefieldAfterEtb() {
        harness.setGraveyard(player1, List.of(new GoldMyr()));
        castAndResolve();

        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Razor Hippogriff"));
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.setGraveyard(player1, List.of(new GoldMyr()));
        castAndResolve();

        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    // ===== Opponent cannot make choice =====

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        harness.setGraveyard(player1, List.of(new GoldMyr()));
        castAndResolve();

        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }
}
