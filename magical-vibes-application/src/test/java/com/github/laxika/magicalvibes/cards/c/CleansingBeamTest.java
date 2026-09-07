package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleansingBeam.class, HillGiant.class, AirElemental.class, Island.class})
class CleansingBeamTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the target and every creature sharing a color with it")
    void damagesTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
