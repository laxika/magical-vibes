package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HiddenNursery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaslemsStonetree.class, KaslemsStrider.class, Forest.class, HiddenNursery.class})
class KaslemsStonetreeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a land from the top six and puts it onto the battlefield tapped")
    void offersLandAndPutsItOntoBattlefieldTapped() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new KaslemsStonetree(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent enteredForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Craft with a Cave returns Kaslem's Strider transformed")
    void craftsWithCaveAndReturnsTransformed() {
        Permanent stonetree = harness.addToBattlefieldAndReturn(player1, new KaslemsStonetree());
        HiddenNursery cave = new HiddenNursery();
        Permanent material = harness.addToBattlefieldAndReturn(player1, cave);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stonetree);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof KaslemsStrider);
    }

    @Test
    void mayDeclineLandAndPutAllLookedAtCardsOnBottom() {
        Forest forest = new Forest();
        KaslemsStonetree nonland = new KaslemsStonetree();
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.castFromHand(player1, new KaslemsStonetree(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyLooksAtSixCardsAndPutsUnchosenCardsBelowUntouchedLibrary() {
        Forest eligible = new Forest();
        List<Card> topSix = List.of(eligible, new KaslemsStonetree(), new KaslemsStonetree(),
                new KaslemsStonetree(), new KaslemsStonetree(), new KaslemsStonetree());
        Forest seventh = new Forest();
        List<Card> library = new ArrayList<>(topSix);
        library.add(seventh);
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new KaslemsStonetree(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topSix.subList(1, 6));
    }

    @Test
    void noLandInTopSixReturnsAllCardsToBottomWithoutChoice() {
        KaslemsStonetree nonland = new KaslemsStonetree();
        harness.setLibrary(player1, List.of(nonland));
        harness.castFromHand(player1, new KaslemsStonetree(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void craftsWithCaveFromGraveyardAndExilesCostsBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KaslemsStonetree());
        HiddenNursery cave = new HiddenNursery();
        harness.setGraveyard(player1, List.of(cave));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.findExiledCard(source.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(cave.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cave);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(source.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(cave.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p ->
                p.isTransformed() && p.getCard() instanceof KaslemsStrider && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotCraftWithOpponentsCaveOrNonCaveLand() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KaslemsStonetree());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new HiddenNursery());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.findExiledCard(source.getCard().getId())).isNull();
    }

    @Test
    void cannotCraftOutsideMainPhase() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KaslemsStonetree());
        harness.addToBattlefield(player1, new HiddenNursery());
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.findExiledCard(source.getCard().getId())).isNull();
    }
}
