package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheCoffers.class, Swamp.class})
class MagusOfTheCoffersTest extends BaseCardTest {

    @Test
    @DisplayName("Adds black mana for each Swamp you control")
    void addsBlackManaForEachControlledSwamp() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCoffers());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        int magusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(magus);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, magusIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no black mana when you control no Swamps")
    void addsNoManaWithNoControlledSwamps() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCoffers());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(magus), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Pays two generic mana and taps Magus of the Coffers")
    void paysActivationCostAndTapsSource() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCoffers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(magus), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(magus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counts tapped Swamps and resolves without using the stack")
    void countsTappedSwampsAndResolvesImmediately() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCoffers());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(magus), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(swamp.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheCoffers());
        magus.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(magus), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(magus.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateAgainWhileTapped() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCoffers());
        harness.addToBattlefield(player1, new Swamp());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int magusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(magus);
        harness.activateAbility(player1, magusIndex, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, magusIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
