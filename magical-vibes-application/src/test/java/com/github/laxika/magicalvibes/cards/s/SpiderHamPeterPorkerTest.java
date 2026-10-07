package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuntMay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderHamPeterPorker.class, GrizzlyBears.class, AuntMay.class})
class SpiderHamPeterPorkerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenOnEnter() {
        harness.setHand(player1, List.of(new SpiderHamPeterPorker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Other listed animal creatures you control get +1/+1")
    void boostsOtherListedAnimals() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spiderHam = addCreatureReady(player1, new SpiderHamPeterPorker());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, spiderHam)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spiderHam)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's listed animal creatures")
    void doesNotBoostOpponentsAnimals() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new SpiderHamPeterPorker());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost creatures without a listed creature type")
    void doesNotBoostUnlistedCreatureTypes() {
        Permanent auntMay = addCreatureReady(player1, new AuntMay());
        addCreatureReady(player1, new SpiderHamPeterPorker());

        assertThat(gqs.getEffectivePower(gd, auntMay)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, auntMay)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering without being cast creates exactly one Food for its controller")
    void createsFoodWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player2, new SpiderHamPeterPorker());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Food can be sacrificed immediately, with life gained on resolution")
    void foodSacrificeIsPaidBeforeLifeGain() {
        harness.enterBattlefieldAndReturn(player1, new SpiderHamPeterPorker());
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, foodIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Food requires two mana and an untapped token to activate")
    void foodRequiresManaAndTapCost() {
        harness.enterBattlefieldAndReturn(player1, new SpiderHamPeterPorker());
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");
        assertThat(food.isTapped()).isFalse();
        harness.assertLife(player1, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        food.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Leaving removes the anthem but does not stop the pending Food trigger")
    void leavingRemovesBoostButFoodTriggerStillResolves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spiderHam = harness.enterBattlefieldAndReturn(player1, new SpiderHamPeterPorker());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, spiderHam));

        harness.assertInGraveyard(player1, "Spider-Ham, Peter Porker");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }
}
