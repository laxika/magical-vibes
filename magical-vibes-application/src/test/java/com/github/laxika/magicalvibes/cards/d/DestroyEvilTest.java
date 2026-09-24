package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestroyEvil.class, AirElemental.class, GloriousAnthem.class, GrizzlyBears.class})
class DestroyEvilTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureWithToughnessAtLeastFour() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(new int[]{0}, creature);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent enchantment = findPermanent(player2, "Glorious Anthem");

        cast(new int[]{1}, enchantment);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void rejectsCreatureWithToughnessLessThanFourForCreatureMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{0}, creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCreatureForEnchantmentMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThatThrownBy(() -> cast(new int[]{1}, creature))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, Permanent target) {
        harness.setHand(player1, List.of(new DestroyEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
