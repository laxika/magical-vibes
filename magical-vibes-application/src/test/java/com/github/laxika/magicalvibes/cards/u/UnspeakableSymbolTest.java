package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DwarvenHold;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnspeakableSymbol.class, GrizzlyBears.class, DwarvenHold.class})
class UnspeakableSymbolTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 3 life and puts a +1/+1 counter on a target creature")
    void paysLifeAndPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can put a counter on an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without enough life")
    void cannotActivateWithoutEnoughLife() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
