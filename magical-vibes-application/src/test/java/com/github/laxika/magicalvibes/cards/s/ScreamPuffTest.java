package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoryMice;
import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScreamPuff.class, ArmoryMice.class, HamletGlutton.class})
class ScreamPuffTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player creates a Food token")
    void combatDamageCreatesFood() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Combat damage that is dealt to a blocker does not create a Food token")
    void combatDamageToCreatureDoesNotCreateFood() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ArmoryMice());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Deathtouch kills a blocker with more toughness than Scream Puff's power")
    void deathtouchKillsLargeBlocker() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HamletGlutton());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hamlet Glutton");
        harness.assertInGraveyard(player1, "Scream Puff");
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The attacking creature's controller creates Food when player two attacks")
    void otherPlayerCreatesFood() {
        Permanent screamPuff = addCreatureReady(player2, new ScreamPuff());
        screamPuff.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(countPermanents(player2, "Food")).isOne();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Food activation requires two mana and does not pay costs on failure")
    void foodRequiresTwoMana() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(food.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Food cannot pay the tap cost of its ability")
    void tappedFoodCannotBeActivated() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        food.tap();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and life is gained only on resolution")
    void foodSacrificePrecedesLifeGain() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, foodIndex, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("The Food token can be sacrificed to gain 3 life")
    void foodCanBeSacrificedForLife() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }
}
