package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreetopSentries.class, GrizzlyBears.class})
class TreetopSentriesTest extends BaseCardTest {

    @Test
    void foragingByExilingThreeGraveyardCardsDrawsACard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castSentries();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    void foragingBySacrificingFoodDrawsACard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent food = addFoodToken();
        castSentries();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningForageDoesNotDrawACard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castSentries();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void acceptingWithoutFoodOrEnoughGraveyardCardsDoesNotDrawOrExile() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        castSentries();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsGraveyardCannotPayForForage() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castSentries();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
    }

    private void castSentries() {
        harness.setHand(player1, List.of(new TreetopSentries()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent addFoodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        return harness.addToBattlefieldAndReturn(player1, food);
    }
}
