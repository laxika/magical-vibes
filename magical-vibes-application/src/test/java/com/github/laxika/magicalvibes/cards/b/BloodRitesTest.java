package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodRites.class, WanderingOnes.class})
class BloodRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice is paid before opponents can respond and damage waits for resolution")
    void sacrificesAtActivation() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Wandering Ones");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Blood Rites");
    }

    @Test
    @DisplayName("The target creature can itself be sacrificed to pay the cost")
    void canSacrificeTargetCreature() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        var target = harness.getPermanentId(player1, "Wandering Ones");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, target);

        harness.assertInGraveyard(player1, "Wandering Ones");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can activate on the opponent's turn and target its controller")
    void canActivateOnOpponentsTurnAndTargetSelf() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("A noncreature enchantment is not a legal damage target")
    void cannotTargetNoncreatureEnchantment() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        var target = harness.getPermanentId(player1, "Blood Rites");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wandering Ones");
        harness.assertNotInGraveyard(player1, "Wandering Ones");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices a creature and deals 2 damage to a player")
    void dealsTwoDamageToPlayer() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("Deals 2 damage to a creature, killing a 1-toughness one")
    void dealsTwoDamageToCreature() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.addToBattlefield(player2, new WanderingOnes());
        var target = harness.getPermanentId(player2, "Wandering Ones");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void requiresCreatureYouControl() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player2, new WanderingOnes());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void requiresMana() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the required red mana")
    void requiresRedMana() {
        harness.addToBattlefield(player1, new BloodRites());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
