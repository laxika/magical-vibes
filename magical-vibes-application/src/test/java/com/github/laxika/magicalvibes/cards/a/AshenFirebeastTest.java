package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshenFirebeast.class, Anarchist.class, AvenFlock.class})
class AshenFirebeastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature without flying")
    void dealsDamageOnlyToCreaturesWithoutFlying() {
        Permanent firebeast = addCreatureReady(player1, new AshenFirebeast());
        Permanent groundCreature = addCreatureReady(player2, new Anarchist());
        Permanent flyingCreature = addCreatureReady(player2, new AvenFlock());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebeast.getMarkedDamage()).isEqualTo(1);
        assertThat(firebeast.isTapped()).isFalse();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
