package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCabbageMerchant.class, Divination.class, GrizzlyBears.class})
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

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
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

    private void createFood() {
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0);
        resolveAllTriggers();
    }
}
