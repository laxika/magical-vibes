package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonstrousEmergence.class, AirElemental.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class})
class MonstrousEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the chosen creature's power at resolution")
    void usesChosenCreaturePowerAtResolution() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonstrousEmergence(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(chosen.getId()), List.of());
        harness.castInstant(player1, 0, chosen.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Uses the power of a revealed creature card")
    void usesRevealedCreatureCardPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        HillGiant revealed = new HillGiant();
        harness.setHand(player1, List.of(new MonstrousEmergence(), revealed));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Cannot be cast without a creature to choose or reveal")
    void requiresCreatureChoiceOrReveal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonstrousEmergence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
