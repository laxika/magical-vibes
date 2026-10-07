package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TraverseValley.class, EvolvingWilds.class, Forest.class})
class TraverseValleyTest extends BaseCardTest {

    @Test
    void unKickedSeekPutsNonbasicLandIntoHand() {
        EvolvingWilds nonbasicLand = new EvolvingWilds();
        Forest basicLand = new Forest();
        harness.setHand(player1, List.of(new TraverseValley()));
        harness.setLibrary(player1, List.of(nonbasicLand, basicLand));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(nonbasicLand.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
    }

    @Test
    void kickedForagePutsNonbasicLandOntoBattlefieldTapped() {
        EvolvingWilds nonbasicLand = new EvolvingWilds();
        Forest basicLand = new Forest();
        harness.setHand(player1, List.of(new TraverseValley()));
        harness.setLibrary(player1, List.of(nonbasicLand, basicLand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent food = addFoodToken();

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, food.getId());
        harness.passBothPriorities();

        Permanent foundLand = findPermanent(player1, "Evolving Wilds");
        assertThat(foundLand.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
    }

    @Test
    void kickedLandEntersFromHandAfterBeingSought() {
        EvolvingWilds land = new EvolvingWilds();
        harness.setHand(player1, List.of(new TraverseValley()));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent food = addFoodToken();

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, food.getId());
        harness.passBothPriorities();

        Permanent foundLand = findPermanent(player1, "Evolving Wilds");
        assertThat(foundLand.getCard().getId()).isEqualTo(land.getId());
        assertThat(foundLand.getEnteredFromZone()).isEqualTo(Zone.HAND);
        assertThat(foundLand.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void forageCanExileThreeGraveyardCardsInsteadOfSacrificingFood() {
        EvolvingWilds land = new EvolvingWilds();
        List<Card> forageCards = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(new TraverseValley()));
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, forageCards);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, List.of(0, 1, 2), true);
        harness.passBothPriorities();

        Permanent foundLand = findPermanent(player1, "Evolving Wilds");
        assertThat(foundLand.getCard().getId()).isEqualTo(land.getId());
        assertThat(foundLand.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Traverse Valley");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotKickWithoutPayingForage() {
        harness.setHand(player1, List.of(new TraverseValley()));
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Traverse Valley");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void noNonbasicLandLeavesLibraryUnchanged() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new TraverseValley()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Traverse Valley");
    }

    @Test
    void seeksExactlyOneMatchingLandWithoutShufflingRemainingCards() {
        Forest first = new Forest();
        EvolvingWilds firstLand = new EvolvingWilds();
        Forest middle = new Forest();
        EvolvingWilds secondLand = new EvolvingWilds();
        Forest last = new Forest();
        List<Card> library = List.of(first, firstLand, middle, secondLand, last);
        harness.setHand(player1, List.of(new TraverseValley()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card sought = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(sought.getId()).isIn(firstLand.getId(), secondLand.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(library.stream()
                        .filter(card -> !card.getId().equals(sought.getId())).toList());
    }

    private Permanent addFoodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        Permanent permanent = harness.addToBattlefieldAndReturn(player1, food);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
