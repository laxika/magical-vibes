package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({CuriousForager.class, Forest.class, GiantGrowth.class, GrizzlyBears.class,
        LlanowarElves.class, Shock.class})
class CuriousForagerTest extends BaseCardTest {

    @Test
    void foragingBySacrificingFoodReturnsAChosenPermanentCardAfterForage() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        Permanent food = addFoodToken();
        castForager();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
    }

    @Test
    void foragingByExilingThreeCardsLeavesTheTargetChoiceUntilAfterExile() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new LlanowarElves(), new Shock(), new Forest()));
        castForager();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    void noPermanentCardMeansTheForageRiderDoesNothing() {
        harness.setGraveyard(player1, List.of(new GiantGrowth()));
        Permanent food = addFoodToken();
        castForager();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getName().equals("Giant Growth"));
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    private void castForager() {
        harness.setHand(player1, List.of(new CuriousForager()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addFoodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        Permanent permanent = new Permanent(food);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
