package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EerieUltimatum.class, Forest.class, GrizzlyBears.class, HolyDay.class, Pacifism.class})
class EerieUltimatumTest extends BaseCardTest {

    @Test
    void returnsAnyNumberOfPermanentCardsWithDifferentNamesSimultaneously() {
        Card bears = new GrizzlyBears();
        Card duplicateBears = new GrizzlyBears();
        Card forest = new Forest();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(bears, duplicateBears, forest, instant));
        castEerieUltimatum();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        PendingInteraction.GraveyardChoice nextChoice = gd.interaction
                .activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(nextChoice).isNotNull();
        assertThat(nextChoice.cardPool()).extracting(Card::getId).containsExactly(forest.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .containsExactlyInAnyOrder(bears.getId(), forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .hasSize(3).contains(duplicateBears.getId(), instant.getId());
    }

    @Test
    void mayReturnZeroCards() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castEerieUltimatum();

        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castEerieUltimatum() {
        harness.castFromHand(player1, new EerieUltimatum(), "{W}{W}{B}{B}{B}{G}{G}");
        harness.passBothPriorities();
    }

    @Test
    void mayStopAfterChoosingOneCard() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(bears, forest));
        castEerieUltimatum();

        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void doesNotReturnOpponentsCardsOrNonpermanentCards() {
        Card instant = new HolyDay();
        Card opposingForest = new Forest();
        harness.setGraveyard(player1, List.of(instant));
        harness.setGraveyard(player2, List.of(opposingForest));
        castEerieUltimatum();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Eerie Ultimatum");
    }

    @Test
    void resolvesWithAnEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        castEerieUltimatum();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Eerie Ultimatum");
    }

    @Test
    void returnedAuraChoosesAnExistingCreatureToEnchant() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Pacifism()));
        castEerieUltimatum();

        harness.handleGraveyardCardChosen(player1, 0);

        PendingInteraction.PermanentChoice attachment = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(attachment).isNotNull();
        assertThat(attachment.validPermanentIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Pacifism"))
                .singleElement().satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(bears.getId()));
        harness.assertNotInGraveyard(player1, "Pacifism");
    }
}
