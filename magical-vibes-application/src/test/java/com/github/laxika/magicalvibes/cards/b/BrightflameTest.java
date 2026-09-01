package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
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

@CardUsed({Brightflame.class, AirElemental.class, HealingSalve.class, HillGiant.class, Island.class})
class BrightflameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to the target and color-sharing creatures, then gains the damage dealt")
    void damagesTargetAndColorSharingCreaturesAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains life only for damage that was actually dealt")
    void lifeGainUsesActualDamageAfterPrevention() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, 1, protectedCreature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
