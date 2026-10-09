package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathforgeShaman.class, JaceTheMindSculptor.class})
class DeathforgeShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it deals no damage")
    void dealsNoDamageWithoutMultikicker() {
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("It deals twice the number of multikicker payments")
    void dealsTwiceTheNumberOfMultikickerPayments() {
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castWithMultikickerPayments(List.of("{R}", "{R}"));
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The ETB trigger cannot target a creature")
    void cannotTargetCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new DeathforgeShaman());
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Choosing the target after entry preserves multikicker payments")
    void deferredTargetChoicePreservesMultikickerPayments() {
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{R}", "{R}"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("One multikicker payment can deal two damage to its controller")
    void canDamageItsController() {
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castWithMultikickerPayments(List.of("{R}"), player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage removes loyalty from a targeted planeswalker")
    void damagesPlaneswalker() {
        var jace = harness.addToBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castWithMultikickerPayments(List.of("{R}"), jace.getId());
        resolveAllTriggers();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger retains its damage after the source leaves")
    void damageSurvivesSourceLeaving() {
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castWithMultikickerPayments(List.of("{R}", "{R}"));
        harness.passBothPriorities();
        var shaman = findPermanent(player1, "Deathforge Shaman");
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        gd.playerGraveyards.get(player1.getId()).add(shaman.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    private void castWithMultikickerPayments(List<String> payments) {
        castWithMultikickerPayments(payments, player2.getId());
    }

    private void castWithMultikickerPayments(List<String> payments, UUID targetId) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
    }
}
