package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanctumGargoyleTest extends BaseCardTest {

    /** Casts Sanctum Gargoyle and resolves the creature so its ETB sets up graveyard targeting. */
    private void castSanctumGargoyle() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SanctumGargoyle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    @Test
    @DisplayName("ETB returns a targeted artifact card from graveyard to hand")
    void etbReturnsArtifactToHand() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("A non-artifact card in the graveyard is not a legal target")
    void nonArtifactNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // Choose nothing — "you may return"
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Empty graveyard produces no trigger")
    void emptyGraveyardNoTrigger() {
        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
