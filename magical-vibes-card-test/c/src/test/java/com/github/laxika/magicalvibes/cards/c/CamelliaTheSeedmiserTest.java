package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.y.YgraEaterOfAll;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CamelliaTheSeedmiser.class, BakersbaneDuo.class, YgraEaterOfAll.class})
class CamelliaTheSeedmiserTest extends BaseCardTest {

    @Test
    void sacrificingFoodCreatesASquirrelToken() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        Permanent food = addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .hasSize(1);
    }

    @Test
    void foragePutsCountersOnOtherSquirrelsYouControl() {
        Permanent camellia = harness.addToBattlefieldAndReturn(player1, new CamelliaTheSeedmiser());
        Permanent squirrel = addSquirrel(player1, "Squirrel");
        Permanent opponentSquirrel = addSquirrel(player2, "Opponent Squirrel");
        harness.setGraveyard(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(camellia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentSquirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void grantsMenaceToOtherSquirrelsYouControl() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        Permanent squirrel = addSquirrel(player1, "Squirrel");
        Permanent opponentSquirrel = addSquirrel(player2, "Opponent Squirrel");

        assertThat(gqs.hasKeyword(gd, squirrel, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSquirrel, Keyword.MENACE)).isFalse();
    }

    @Test
    void cannotActivateWithoutFoodOrThreeGraveyardCards() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        harness.setGraveyard(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void graveyardForageIsPaidBeforeEitherPlayerCanRespond() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        Permanent squirrel = addSquirrel(player1, "Squirrel");
        harness.setGraveyard(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void squirrelCreatedByForageGetsCounterFromTheActivatedAbility() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        Permanent food = addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
    }

    @Test
    void sacrificingCreatureMadeFoodByYgraCreatesSquirrel() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        harness.addToBattlefield(player1, new YgraEaterOfAll());
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new BakersbaneDuo());
        duo.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, 0, null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duo);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .hasSize(1);
    }

    @Test
    void canChooseGraveyardForageWhenFoodIsAlsoAvailable() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        Permanent food = addFoodToken(player1);
        Card retained = new BakersbaneDuo();
        Card first = new BakersbaneDuo();
        Card second = new BakersbaneDuo();
        Card third = new BakersbaneDuo();
        harness.setGraveyard(player1, List.of(retained, first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "Exile three graveyard cards");
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(food);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canChooseFoodForageWhenGraveyardCardsAreAlsoAvailable() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        Permanent food = addFoodToken(player1);
        harness.setGraveyard(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "Sacrifice a Food");
        harness.handlePermanentChosen(player1, food.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.stack).hasSize(2);
    }

    private Permanent addFoodToken(Player player) {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        Permanent permanent = harness.addToBattlefieldAndReturn(player, food);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addSquirrel(Player player, String name) {
        Card squirrel = new Card();
        squirrel.setName(name);
        squirrel.setType(CardType.CREATURE);
        squirrel.setPower(1);
        squirrel.setToughness(1);
        squirrel.setToken(true);
        squirrel.setSubtypes(List.of(CardSubtype.SQUIRREL));

        Permanent permanent = harness.addToBattlefieldAndReturn(player, squirrel);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
