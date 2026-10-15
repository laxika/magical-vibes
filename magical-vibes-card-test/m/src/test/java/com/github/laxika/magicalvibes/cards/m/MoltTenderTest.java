package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltTender.class})
class MoltTenderTest extends BaseCardTest {

    @Test
    void millsOneCard() {
        Permanent tender = addReadyTender();
        GameData gd = harness.getGameData();
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tender.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void exilesAnyGraveyardCardAndAddsChosenMana() {
        Permanent tender = addReadyTender();
        Card graveyardCard = new MoltTender();
        harness.setGraveyard(player1, List.of(graveyardCard));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(tender.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void cannotActivateManaAbilityWithoutAGraveyardCard() {
        addReadyTender();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    void cannotUseOpponentsGraveyardToPayManaAbilityCost() {
        Permanent tender = addReadyTender();
        Card opponentCard = new MoltTender();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCard));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        assertThat(tender.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothTapAbilitiesAreUnavailableWithSummoningSickness() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new MoltTender());
        Card graveyardCard = new MoltTender();
        harness.setGraveyard(player1, List.of(graveyardCard));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(tender.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millingUsesTheStackAndOnlyMillsTheControllersLibrary() {
        addReadyTender();
        Card topCard = new MoltTender();
        Card nextCard = new MoltTender();
        Card opponentCard = new MoltTender();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentCard);
    }

    @Test
    void canMillAnEmptyLibrary() {
        Permanent tender = addReadyTender();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tender.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityResolvesWithoutUsingTheStackAndExilesOnlyTheChosenCard() {
        Permanent tender = addReadyTender();
        Card firstCard = new MoltTender();
        Card chosenCard = new MoltTender();
        harness.setGraveyard(player1, List.of(firstCard, chosenCard));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(tender.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosenCard);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    private Permanent addReadyTender() {
        return addCreatureReady(player1, new MoltTender());
    }
}
