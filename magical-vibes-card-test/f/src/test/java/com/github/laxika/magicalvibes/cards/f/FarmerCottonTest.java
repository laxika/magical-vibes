package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FarmerCotton.class})
class FarmerCottonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X Halflings and X Food tokens")
    void createsXHalflingsAndFood() {
        cast(2);

        List<Permanent> halflings = findPermanents(player1, "Halfling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        List<Permanent> food = findPermanents(player1, "Food");

        assertThat(halflings).hasSize(2);
        assertThat(halflings).allSatisfy(halfling -> {
            assertThat(halfling.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(halfling.getCard().getSubtypes()).contains(CardSubtype.HALFLING);
            assertThat(halfling.getEffectivePower()).isEqualTo(1);
            assertThat(halfling.getEffectiveToughness()).isEqualTo(1);
        });
        assertThat(food).hasSize(2);
        assertThat(food).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        });
    }

    @Test
    @DisplayName("With X=0, creates no tokens")
    void xZeroCreatesNoTokens() {
        cast(0);

        assertThat(findPermanents(player1, "Halfling")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    private void cast(int xValue) {
        castOntoStack(xValue);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Food can be sacrificed immediately for three life, paid before resolution")
    void foodActivationPaysCostsAndGainsLifeOnResolution() {
        cast(2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, foodIndex, null, null);

        assertThat(findPermanents(player1, "Food")).hasSize(1).doesNotContain(food);
        harness.assertLife(player1, 10);
        resolveAllTriggers();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 12);
        assertThat(findPermanents(player1, "Halfling")).hasSize(2);
    }

    @Test
    @DisplayName("Tapped Food cannot activate its tap ability")
    void tappedFoodCannotActivate() {
        cast(1);
        Permanent food = findPermanent(player1, "Food");
        food.tap();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Food")).containsExactly(food);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Food requires two mana and is not sacrificed when payment fails")
    void foodRequiresTwoMana() {
        cast(1);
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Food")).containsExactly(food);
        assertThat(food.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Entering without being cast uses zero for X")
    void enteringWithoutCastingCreatesNoTokens() {
        harness.enterBattlefieldAndReturn(player1, new FarmerCotton());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Farmer Cotton");
        assertThat(findPermanents(player1, "Halfling")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger retains X after Farmer Cotton leaves")
    void triggerRetainsXAfterSourceLeaves() {
        castOntoStack(3);
        harness.passBothPriorities();
        Permanent farmer = findPermanent(player1, "Farmer Cotton");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, farmer));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Farmer Cotton");
        harness.assertInHand(player1, "Farmer Cotton");
        assertThat(findPermanents(player1, "Halfling")).hasSize(3);
        assertThat(findPermanents(player1, "Food")).hasSize(3);
        assertThat(findPermanents(player2, "Halfling")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
    }

    private void castOntoStack(int xValue) {
        harness.setHand(player1, List.of(new FarmerCotton()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        harness.castCreature(player1, 0, xValue);
    }
}
