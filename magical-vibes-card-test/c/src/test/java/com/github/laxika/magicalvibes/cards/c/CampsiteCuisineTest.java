package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YunaGrandSummoner;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CampsiteCuisine.class, Food.class, GrizzlyBears.class, YunaGrandSummoner.class})
class CampsiteCuisineTest extends BaseCardTest {

    @Test
    void createsFoodWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new CampsiteCuisine());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void createsFoodWhenLegendaryCreatureEnters() {
        harness.addToBattlefield(player1, new CampsiteCuisine());
        harness.enterBattlefieldAndReturn(player1, new YunaGrandSummoner());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void sacrificesFoodsToBoostUpToThatManyAttackingCreatures() {
        harness.addToBattlefield(player1, new CampsiteCuisine());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstFood = harness.addToBattlefieldAndReturn(player1, new Food());
        Permanent secondFood = harness.addToBattlefieldAndReturn(player1, new Food());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstFood.getId(), secondFood.getId()));

        harness.handlePermanentChosen(player1, firstAttacker.getId());
        harness.handlePermanentChosen(player1, secondAttacker.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, firstAttacker)).isEqualTo(5);
        assertThat(firstAttacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(firstAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstFood, secondFood);
    }
}
