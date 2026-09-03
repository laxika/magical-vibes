package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BushyBodyguard.class, GrizzlyBears.class})
class BushyBodyguardTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new BushyBodyguard()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    void mayForageByExilingThreeGraveyardCardsAndPutCountersOnIt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);
        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mayForageBySacrificingFoodAndPutCountersOnIt() {
        Permanent food = addFoodToken();
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void choosesBetweenExilingCardsAndSacrificingFoodWhenBothAreAvailable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent food = addFoodToken();
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Sacrifice a Food.");
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void decliningForageDoesNotPutCountersOnIt() {
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castBodyguard() {
        harness.setHand(player1, List.of(new BushyBodyguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof BushyBodyguard)
                .findFirst()
                .orElseThrow();
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
