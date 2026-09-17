package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArkOfBlight;
import com.github.laxika.magicalvibes.cards.c.CarrionFeeder;
import com.github.laxika.magicalvibes.cards.d.DawnElemental;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnspeakableSymbol.class, DawnElemental.class, ArkOfBlight.class, CarrionFeeder.class})
class UnspeakableSymbolTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 3 life and puts a +1/+1 counter on a target creature")
    void paysLifeAndPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player1, new DawnElemental());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can put a counter on an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player2, new DawnElemental());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate without enough life")
    void cannotActivateWithoutEnoughLife() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player1, new DawnElemental());
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArkOfBlight());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can activate the ability multiple times")
    void canActivateMultipleTimes() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player1, new DawnElemental());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(4);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Pays life but does not put a counter on a creature that leaves before resolution")
    void targetLeavingBeforeResolution() {
        harness.addToBattlefield(player1, new UnspeakableSymbol());
        Permanent target = addCreatureReady(player1, new DawnElemental());
        Permanent feeder = addCreatureReady(player1, new CarrionFeeder());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dawn Elemental");
    }
}
