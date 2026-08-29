package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoodedAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Hooded Assassin")
    void counterModePutsCounterOnItself() {
        castAssassin(0, null);

        Permanent assassin = findPermanent(player1, "Hooded Assassin");
        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, assassin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Destroy mode destroys a creature dealt damage this turn")
    void destroyModeDestroysDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        damageTarget(target);

        castAssassin(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Destroy mode rejects a creature not dealt damage this turn")
    void destroyModeRejectsUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new HoodedAssassin()));
        addAssassinMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature that was dealt damage this turn");
    }

    private void castAssassin(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new HoodedAssassin()));
        addAssassinMana();
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void damageTarget(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addAssassinMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
