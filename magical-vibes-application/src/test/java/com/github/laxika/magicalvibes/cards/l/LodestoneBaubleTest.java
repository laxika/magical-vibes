package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LodestoneBauble.class, Forest.class, Island.class, HillGiant.class, GrizzlyBears.class})
class LodestoneBaubleTest extends BaseCardTest {

    private Permanent addBauble() {
        Permanent bauble = addCreatureReady(player1, new LodestoneBauble());
        harness.addMana(player1, ManaColor.GREEN, 1);
        return bauble;
    }

    private int baubleIndex(Permanent bauble) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(bauble);
    }

    @Test
    @DisplayName("Puts the targeted basic lands from your own graveyard on top of your library, last chosen on top")
    void ownGraveyardLandsGoOnTopOfLibrary() {
        Permanent bauble = addBauble();

        Card forest = new Forest();
        Card island = new Island();
        harness.setGraveyard(player1, new ArrayList<>(List.of(forest, island)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant(), new GrizzlyBears())));

        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                List.of(forest.getId(), island.getId()));
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(0).getId()).isEqualTo(island.getId());
        assertThat(library.get(1).getId()).isEqualTo(forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(forest.getId(), island.getId());
    }

    @Test
    @DisplayName("The graveyard's owner draws a card at the beginning of the next turn's upkeep")
    void graveyardOwnerDrawsAtNextUpkeep() {
        Permanent bauble = addBauble();

        Card forest = new Forest();
        harness.setGraveyard(player2, new ArrayList<>(List.of(forest)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HillGiant(), new GrizzlyBears())));

        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0, List.of(forest.getId()));
        harness.passBothPriorities();

        // The card moved to its own owner's library, and the delayed draw belongs to that owner.
        assertThat(gd.playerDecks.get(player2.getId()).get(0).getId()).isEqualTo(forest.getId());
        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).get(handBefore).getId()).isEqualTo(forest.getId());
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    void allowsNoTargets() {
        Permanent bauble = addBauble();

        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bauble.getCard().getId());
    }

    @Test
    void offersBasicLandsFromEitherGraveyard() {
        Permanent bauble = addBauble();

        Card ownForest = new Forest();
        Card opponentIsland = new Island();
        Card nonBasic = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownForest, nonBasic));
        harness.setGraveyard(player2, List.of(opponentIsland));

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, bauble.getCard(), bauble.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), baubleIndex(bauble));

        assertThat(response.validGraveyardCardIds())
                .containsExactlyInAnyOrder(ownForest.getId(), opponentIsland.getId());
    }

    @Test
    void reportsOptionalTargetRange() {
        Permanent bauble = addBauble();

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, bauble.getCard(), bauble.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), baubleIndex(bauble));

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.minTargets()).isZero();
        softly.assertThat(response.maxTargets()).isEqualTo(4);
        softly.assertAll();
    }

    @Test
    @DisplayName("Only basic land cards are legal targets")
    void rejectsNonBasicLandTarget() {
        Permanent bauble = addBauble();

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        int index = baubleIndex(bauble);
        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("All targets must come from a single player's graveyard")
    void rejectsTargetsSpanningTwoGraveyards() {
        Permanent bauble = addBauble();

        Card mine = new Forest();
        Card theirs = new Island();
        harness.setGraveyard(player1, new ArrayList<>(List.of(mine)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(theirs)));

        int index = baubleIndex(bauble);
        List<UUID> targets = List.of(mine.getId(), theirs.getId());
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At most four cards may be targeted")
    void rejectsMoreThanFourTargets() {
        Permanent bauble = addBauble();

        List<Card> lands = new ArrayList<>(List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Island()));
        harness.setGraveyard(player1, new ArrayList<>(lands));

        int index = baubleIndex(bauble);
        List<UUID> targets = lands.stream().map(Card::getId).toList();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }
}
