package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheCabbageMerchant.class, Divination.class, GrizzlyBears.class, Gingerbrute.class})
class TheCabbageMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food when an opponent casts a noncreature spell")
    void createsFoodWhenOpponentCastsNoncreatureSpell() {
        addCreatureReady(player1, new TheCabbageMerchant());
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Does not create a Food when an opponent casts a creature spell")
    void doesNotCreateFoodWhenOpponentCastsCreatureSpell() {
        addCreatureReady(player1, new TheCabbageMerchant());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Sacrifices a Food when a creature deals combat damage to you")
    void sacrificesFoodWhenCreatureDealsCombatDamageToYou() {
        addCreatureReady(player1, new TheCabbageMerchant());
        addCreatureReady(player2, new GrizzlyBears());
        createFood();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Tapping two Foods adds one mana of the chosen color")
    void tappingTwoFoodsAddsAnyColorMana() {
        Permanent merchant = addCreatureReady(player1, new TheCabbageMerchant());
        createFood();
        createFood();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Food")).allMatch(Permanent::isTapped);
        assertThat(merchant.isTapped()).isFalse();
    }

    @Test
    void ownNoncreatureSpellDoesNotCreateFood() {
        addCreatureReady(player1, new TheCabbageMerchant());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void foodCanBeSacrificedToGainThreeLife() {
        addCreatureReady(player1, new TheCabbageMerchant());
        createFood();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void combatDamageDoesNotSacrificeNontokenFood() {
        addCreatureReady(player1, new TheCabbageMerchant());
        Permanent food = addCreatureReady(player1, new Gingerbrute());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(findPermanent(player1, "Gingerbrute")).isSameAs(food);
        harness.assertNotInGraveyard(player1, "Gingerbrute");
    }

    @Test
    void eachDamagingCreatureCausesASeparateFoodSacrifice() {
        addCreatureReady(player1, new TheCabbageMerchant());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        createFood();
        createFood();

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMultiplePermanentsChosen(player1,
                    List.of(findPermanents(player1, "Food").getFirst().getId()));
            resolveAllTriggers();
        }

        harness.assertLife(player1, 16);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void manaAbilityRequiresTwoUntappedFoods() {
        addCreatureReady(player1, new TheCabbageMerchant());
        createFood();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Food").isTapped()).isFalse();

        createFood();
        findPermanents(player1, "Food").getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Food").get(1).isTapped()).isFalse();
    }

    @Test
    void manaAbilityCanTapNontokenFoodsWhileMerchantIsTapped() {
        Permanent merchant = addCreatureReady(player1, new TheCabbageMerchant());
        Permanent firstFood = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        Permanent secondFood = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        merchant.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(firstFood.isTapped()).isTrue();
        assertThat(secondFood.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Gingerbrute");
    }

    @Test
    void manaAbilityCannotTapAnOpponentsFood() {
        addCreatureReady(player1, new TheCabbageMerchant());
        Permanent ownFood = addCreatureReady(player1, new Gingerbrute());
        Permanent opposingFood = addCreatureReady(player2, new Gingerbrute());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ownFood.isTapped()).isFalse();
        assertThat(opposingFood.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void createFood() {
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0);
        resolveAllTriggers();
    }
}
