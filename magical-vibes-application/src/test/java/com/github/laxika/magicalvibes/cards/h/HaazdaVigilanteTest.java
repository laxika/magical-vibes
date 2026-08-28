package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HaazdaVigilante.class, GrizzlyBears.class, HillGiant.class})
class HaazdaVigilanteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a qualifying creature you control")
    void etbPutsCounterOnQualifyingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVigilante(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack puts a +1/+1 counter on a qualifying creature you control")
    void attackPutsCounterOnQualifyingCreature() {
        addCreatureReady(player1, new HaazdaVigilante());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent creature or a creature with power greater than two")
    void targetMustBeControlledAndHavePowerAtMostTwo() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HaazdaVigilante()));
        addVigilanteMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");

        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        UUID hillGiantId = hillGiant.getId();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, hillGiantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    private void castVigilante(UUID targetId) {
        harness.setHand(player1, List.of(new HaazdaVigilante()));
        addVigilanteMana();
        harness.castCreature(player1, 0, targetId);
    }

    private void addVigilanteMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
