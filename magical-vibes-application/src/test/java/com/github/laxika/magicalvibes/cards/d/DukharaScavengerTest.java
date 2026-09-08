package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DukharaScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put an artifact or creature card from the graveyard on top of the library")
    void etbPutsArtifactOrCreatureOnTopOfLibrary() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new Shock(), new Bonesplitter(), new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());
        castScavenger();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Bonesplitter");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the ETB leaves the graveyard unchanged")
    void etbCanBeDeclined() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());
        castScavenger();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not offer non-artifact, non-creature cards")
    void etbDoesNotOfferOtherCards() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        harness.setLibrary(player1, new ArrayList<>());
        castScavenger();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    private void castScavenger() {
        harness.setHand(player1, List.of(new DukharaScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
