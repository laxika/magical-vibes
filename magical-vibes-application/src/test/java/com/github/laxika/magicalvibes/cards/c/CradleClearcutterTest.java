package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CradleClearcutter.class})
class CradleClearcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype cast makes the tap ability produce mana equal to prototype power")
    void prototypeTapProducesManaEqualToPrototypePower() {
        harness.setHand(player1, List.of(new CradleClearcutter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent clearcutter = findPermanent(player1, "Cradle Clearcutter");
        clearcutter.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability uses the creature's current power")
    void tapUsesCurrentPower() {
        Permanent clearcutter = addCreatureReady(player1, new CradleClearcutter());
        clearcutter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void normalCastProducesThreeGreenManaImmediately() {
        harness.setHand(player1, List.of(new CradleClearcutter()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent clearcutter = findPermanent(player1, "Cradle Clearcutter");
        clearcutter.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(clearcutter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void prototypeUsesPowerIncludingCounters() {
        harness.setHand(player1, List.of(new CradleClearcutter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent clearcutter = findPermanent(player1, "Cradle Clearcutter");
        clearcutter.setSummoningSick(false);
        clearcutter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(clearcutter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroPowerProducesNoManaButStillTaps() {
        Permanent clearcutter = addCreatureReady(player1, new CradleClearcutter());
        clearcutter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(clearcutter.isTapped()).isTrue();
    }

    @Test
    void negativePowerProducesNoManaAndDoesNotRemoveExistingMana() {
        Permanent clearcutter = addCreatureReady(player1, new CradleClearcutter());
        clearcutter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(clearcutter.isTapped()).isTrue();
    }

    @Test
    void summoningSickCreatureCannotActivateTapAbility() {
        Permanent clearcutter = harness.addToBattlefieldAndReturn(player1, new CradleClearcutter());
        clearcutter.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(clearcutter.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotActivateAgain() {
        Permanent clearcutter = addCreatureReady(player1, new CradleClearcutter());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(clearcutter.isTapped()).isTrue();
    }
}
