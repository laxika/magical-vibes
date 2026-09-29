package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AstridPeth.class, Clue.class, Food.class, Forest.class})
class AstridPethTest extends BaseCardTest {

    @Test
    void entersAndAttacksCreateFood() {
        Permanent astrid = harness.enterBattlefieldAndReturn(player1, new AstridPeth());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);

        astrid.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(2);
    }

    @Test
    void sacrificingFoodMakesAstridExplore() {
        harness.addToBattlefield(player1, new AstridPeth());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new Food());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    void sacrificingClueMakesAstridExplore() {
        harness.addToBattlefield(player1, new AstridPeth());
        Permanent clue = harness.addToBattlefieldAndReturn(player1, new Clue());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clue);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }
}
