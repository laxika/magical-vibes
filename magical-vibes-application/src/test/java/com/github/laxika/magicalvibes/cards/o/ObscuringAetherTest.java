package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HerdchaserDragon;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObscuringAether.class, HerdchaserDragon.class, HillGiant.class})
class ObscuringAetherTest extends BaseCardTest {

    @Test
    void reducesFaceDownCreatureSpellCost() {
        harness.addToBattlefield(player1, new ObscuringAether());
        harness.setHand(player1, List.of(new HerdchaserDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceFaceUpCreatureSpellCost() {
        harness.addToBattlefield(player1, new ObscuringAether());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void turnsItselfFaceDownAsA2By2Creature() {
        Permanent aether = harness.addToBattlefieldAndReturn(player1, new ObscuringAether());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(aether.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, aether)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aether)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
