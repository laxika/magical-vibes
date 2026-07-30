package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VesselOfEndlessRestTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a chosen card from the controller's graveyard on the bottom of their library")
    void etbBottomsOwnGraveyardCard() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.setGraveyard(player1, List.of(new GhostWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Ghost Warden");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName()).isEqualTo("Ghost Warden");
    }

    @Test
    @DisplayName("ETB can bottom a card from an opponent's graveyard, into that opponent's library")
    void etbBottomsOpponentGraveyardCard() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does nothing when all graveyards are empty")
    void etbWithEmptyGraveyards() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("{T} ability prompts for a color and adds one mana of it")
    void tapAbilityAddsChosenColor() {
        harness.addToBattlefield(player1, new VesselOfEndlessRest());
        Permanent vessel = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(vessel.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
