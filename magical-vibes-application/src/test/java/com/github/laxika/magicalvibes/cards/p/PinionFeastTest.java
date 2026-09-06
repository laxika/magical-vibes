package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PinionFeast.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class PinionFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with flying and bolsters the least-tough creature")
    void destroysFlyingCreatureAndBolsters() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        Permanent leastToughCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent largerCreature = addCreatureReady(player1, new HillGiant());

        cast(target.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new AirElemental());
        Permanent nonFlyingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PinionFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonFlyingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new PinionFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
