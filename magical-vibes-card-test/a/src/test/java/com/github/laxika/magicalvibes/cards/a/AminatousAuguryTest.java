package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AminatousAugury.class, Forest.class, ElvishMystic.class, PropheticPrism.class})
class AminatousAuguryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an optional land onto the battlefield and permits one later spell per type")
    void choosesLandAndOneSpellPerType() {
        Forest forest = new Forest();
        ElvishMystic firstMystic = new ElvishMystic();
        ElvishMystic secondMystic = new ElvishMystic();
        PropheticPrism prism = new PropheticPrism();
        cast(List.of(forest, firstMystic, secondMystic, prism));

        assertThat(activeChoice().offeredCardType()).isEqualTo(CardType.LAND);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(harness.getPermanentId(player1, "Forest")).isNotNull();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();

        harness.castFromExile(player1, firstMystic.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(harness.getPermanentId(player1, "Elvish Mystic")).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, secondMystic.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.castFromExile(player1, prism.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == prism);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secondMystic);
    }

    @Test
    @DisplayName("Declining the land leaves all cards exiled without requiring spell choices")
    void decliningChoicesLeavesCardsExiled() {
        Forest forest = new Forest();
        ElvishMystic mystic = new ElvishMystic();
        PropheticPrism prism = new PropheticPrism();
        cast(List.of(forest, mystic, prism));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(forest, mystic, prism);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Without an exiled land, Augury finishes resolving before any spell is cast")
    void noLandDoesNotPromptForImmediateCasting() {
        ElvishMystic mystic = new ElvishMystic();
        cast(List.of(mystic));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(mystic);

        harness.castFromExile(player1, mystic.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == mystic);
    }

    @Test
    @DisplayName("Only the top eight cards are exiled, and only one land can be put onto the battlefield")
    void exilesExactlyEightCardsAndPutsOnlyOneLandOntoBattlefield() {
        List<Card> library = IntStream.range(0, 9)
                .mapToObj(i -> (Card) new Forest()).toList();
        cast(library);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(8));
        assertThat(activeChoice().validCardIds()).containsExactlyInAnyOrderElementsOf(
                library.subList(0, 8).stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, List.of(library.getFirst().getId()));

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(1, 8));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("An empty library produces no choices")
    void emptyLibraryFinishesNormally() {
        cast(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Putting a land onto the battlefield is allowed after the normal land play")
    void landChoiceDoesNotUseALandPlay() {
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Forest forest = new Forest();
        cast(List.of(forest));

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Cards already in exile are not included in Augury's choices")
    void doesNotOfferPreviouslyExiledCards() {
        Forest oldForest = new Forest();
        ElvishMystic oldMystic = new ElvishMystic();
        harness.setExile(player1, List.of(oldForest, oldMystic));
        Forest forest = new Forest();
        cast(List.of(forest));

        assertThat(activeChoice().validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, oldMystic.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(oldForest, oldMystic, forest);
    }

    private PendingInteraction.AminatousAuguryChoice activeChoice() {
        return (PendingInteraction.AminatousAuguryChoice) gd.interaction.activeInteraction();
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new AminatousAugury(), "{6}{U}{U}");
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
    }
}
