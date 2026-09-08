package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CamelliaTheSeedmiser.class, GrizzlyBears.class})
class CamelliaTheSeedmiserTest extends BaseCardTest {

    @Test
    void sacrificingFoodCreatesASquirrelToken() {
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        Permanent food = addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
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
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
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

    private Permanent addFoodToken(Player player) {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        Permanent permanent = new Permanent(food);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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

        Permanent permanent = new Permanent(squirrel);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
