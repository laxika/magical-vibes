package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CathedralSanctifier;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VesselOfEndlessRest.class, CathedralSanctifier.class, MoorlandInquisitor.class})
class VesselOfEndlessRestTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a chosen card from the controller's graveyard on the bottom of their library")
    void etbBottomsOwnGraveyardCard() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.setGraveyard(player1, List.of(new CathedralSanctifier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Cathedral Sanctifier");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName()).isEqualTo("Cathedral Sanctifier");
    }

    @Test
    @DisplayName("ETB can bottom a card from an opponent's graveyard, into that opponent's library")
    void etbBottomsOpponentGraveyardCard() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.setGraveyard(player2, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player2, "Moorland Inquisitor");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Moorland Inquisitor");
    }

    @Test
    @DisplayName("ETB does nothing when all graveyards are empty")
    void etbWithEmptyGraveyards() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

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

    @Test
    @DisplayName("An ETB trigger with no legal graveyard target is removed before priority")
    void emptyGraveyardsLeaveNoTriggerOnStack() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vessel of Endless Rest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The graveyard card choice cannot be declined")
    void cannotDeclineGraveyardCard() {
        harness.setHand(player1, List.of(new VesselOfEndlessRest()));
        harness.setGraveyard(player1, List.of(new CathedralSanctifier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertNotInGraveyard(player1, "Cathedral Sanctifier");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The mana ability immediately produces any chosen color without using the stack")
    void manaAbilityProducesEachColor(ManaColor color) {
        harness.addToBattlefield(player1, new VesselOfEndlessRest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanent(player1, "Vessel of Endless Rest").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
