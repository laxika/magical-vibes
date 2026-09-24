package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
