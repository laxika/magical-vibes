package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EatenAlive;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.ThoughtKnotSeer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiphonInsight.class, GrizzlyBears.class, LightningBolt.class, EatenAlive.class, ThoughtKnotSeer.class, Forest.class})
class SiphonInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one of an opponent's top two cards face down and bottoms the other")
    void exilesOneTopCardAndBottomsTheOther() {
        Card chosenCard = new LightningBolt();
        Card bottomCard = new GrizzlyBears();
        castSiphonInsight(List.of(chosenCard, bottomCard));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(chosenCard, bottomCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bottomCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.exiledCards).filteredOn(entry -> entry.card().getId().equals(chosenCard.getId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.exilePlayPermissions).containsEntry(chosenCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosenCard.getId());
    }

    @Test
    @DisplayName("The exiled card can be cast with any color of mana")
    void exiledCardCanBeCastWithAnyMana() {
        Card chosenCard = new LightningBolt();
        castSiphonInsight(List.of(chosenCard, new GrizzlyBears()));

        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, chosenCard.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SiphonInsight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Flashback resolves and exiles Siphon Insight")
    void flashbackResolvesAndExiles() {
        Card chosenCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SiphonInsight()));
        harness.setLibrary(player2, List.of(chosenCard, new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Siphon Insight"));
    }

    @Test
    void canChooseSecondCardAndLeavesDeeperCardsInOrder() {
        Card first = new GrizzlyBears();
        Card chosen = new LightningBolt();
        Card third = new GrizzlyBears();
        Card fourth = new LightningBolt();
        castSiphonInsight(List.of(first, chosen, third, fourth));

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, fourth, first);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    @Test
    void exilesOnlyCardWhenLibraryHasOneCard() {
        Card chosen = new GrizzlyBears();
        castSiphonInsight(List.of(chosen));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    @Test
    void resolvesWithoutChoiceWhenLibraryIsEmpty() {
        castSiphonInsight(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Siphon Insight");
    }

    @Test
    void exiledCreatureStillRequiresSorceryTiming() {
        Card chosen = new GrizzlyBears();
        castSiphonInsight(List.of(chosen));
        harness.handleCardChosen(player1, 0);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(chosen);
    }

    @Test
    void canPlayExiledLandAndItUsesNormalLandAllowance() {
        Card chosen = new Forest();
        castSiphonInsight(List.of(chosen));
        harness.handleCardChosen(player1, 0);

        harness.castFromExile(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(chosen);
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anyColorPermissionDoesNotPayColorlessManaRequirement() {
        Card chosen = new ThoughtKnotSeer();
        castSiphonInsight(List.of(chosen));
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(chosen);
    }

    @Test
    void canCastExiledSpellByPayingItsAdditionalManaCost() {
        Card chosen = new EatenAlive();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiphonInsight(List.of(chosen));
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castFromExile(player1, chosen.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    private void castSiphonInsight(List<Card> opponentLibrary) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, opponentLibrary);
        harness.setHand(player1, List.of(new SiphonInsight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
