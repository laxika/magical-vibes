package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnnaturalSummons.class, Forest.class, GrizzlyBears.class})
class UnnaturalSummonsTest extends BaseCardTest {

    @Test
    void nonStartingPlayerPaysOneLessAndManifestsDreadWithRebound() {
        gd.activePlayerId = player2.getId();
        UnnaturalSummons card = new UnnaturalSummons();
        GrizzlyBears manifestedCard = new GrizzlyBears();
        Forest graveyardCard = new Forest();
        harness.setHand(player2, List.of(card));
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player2, 0, 0);

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent ->
                permanent.isManifested() && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCard);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void startingPlayerDoesNotGetTheCostReduction() {
        UnnaturalSummons card = new UnnaturalSummons();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void startingPlayerPaysFullCostAndCanManifestALand() {
        UnnaturalSummons spell = new UnnaturalSummons();
        Forest land = new Forest();
        GrizzlyBears other = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(land, other));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isManifested() && permanent.getCard().getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void singleCardLibraryManifestsItsOnlyCard() {
        UnnaturalSummons spell = new UnnaturalSummons();
        GrizzlyBears onlyCard = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isManifested() && permanent.getCard().getId().equals(onlyCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void emptyLibraryStillReboundsAndSecondCastGoesToGraveyard() {
        UnnaturalSummons spell = new UnnaturalSummons();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void costReductionCannotReplaceBlueMana() {
        gd.activePlayerId = player2.getId();
        harness.setHand(player2, List.of(new UnnaturalSummons()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
