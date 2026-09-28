package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
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

@CardUsed({SeatowerImprisonment.class, SoldiersOfTheWatch.class, BottleGnomes.class, GrizzlyBears.class})
class SeatowerImprisonmentTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Soldiers of the Watch and locks an opposing creature")
    void conjuresSoldiersAndLocksCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SeatowerImprisonment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Soldiers of the Watch").getCard().isToken()).isFalse();
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Prevents activated abilities of the enchanted creature")
    void preventsActivatedAbilities() {
        Permanent target = addCreatureReady(player2, new BottleGnomes());

        harness.setHand(player1, List.of(new SeatowerImprisonment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot enchant a creature you control")
    void cannotEnchantOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SeatowerImprisonment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }
}
