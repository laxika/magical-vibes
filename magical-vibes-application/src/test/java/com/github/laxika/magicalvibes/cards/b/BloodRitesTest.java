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
