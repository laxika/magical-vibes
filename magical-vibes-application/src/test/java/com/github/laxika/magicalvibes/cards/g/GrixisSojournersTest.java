package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrixisSojournersTest extends BaseCardTest {

    // ===== Death trigger: you may exile target card from a graveyard =====

    @Test
    @DisplayName("When it dies, exiles a chosen card from a graveyard")
    void diesExilesChosenGraveyardCard() {
        harness.addToBattlefield(player1, new GrixisSojourners());
        harness.setGraveyard(player2, List.of(new LightningBolt()));
        UUID baitId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();

        killWithFlameJavelin();
        harness.passBothPriorities(); // resolve the death trigger -> graveyard exile choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(baitId));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Death trigger may exile nothing — declining leaves graveyards intact")
    void diesMayExileNothing() {
        harness.addToBattlefield(player1, new GrixisSojourners());
        harness.setGraveyard(player2, List.of(new LightningBolt()));

        killWithFlameJavelin();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Death trigger cannot exile a card that is not in any graveyard")
    void diesRejectsNonGraveyardTarget() {
        harness.addToBattlefield(player1, new GrixisSojourners());
        harness.setGraveyard(player2, List.of(new LightningBolt()));

        killWithFlameJavelin();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cycling reflexive trigger: exile a graveyard card, then draw =====

    @Test
    @DisplayName("Cycling exiles a chosen graveyard card and draws a card")
    void cyclingExilesGraveyardCardAndDraws() {
        harness.setHand(player1, List.of(new GrixisSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new LightningBolt()));
        UUID baitId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(baitId));

        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Lightning Bolt"));
        // The cycling draw still happens: Grixis Sojourners discarded, the library card drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grixis Sojourners"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cycling may exile nothing — declining still draws a card")
    void cyclingMayExileNothingStillDraws() {
        harness.setHand(player1, List.of(new GrixisSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Lightning Bolt"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private void killWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID grixisId = harness.getPermanentId(player1, "Grixis Sojourners");
        harness.castInstant(player2, 0, grixisId);
        harness.passBothPriorities(); // Flame Javelin resolves -> Grixis dies -> death trigger onto stack
    }
}
