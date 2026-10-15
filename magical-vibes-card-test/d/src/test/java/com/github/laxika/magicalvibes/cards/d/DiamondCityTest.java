package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GunnerConscript;
import com.github.laxika.magicalvibes.cards.s.SylvanAwakening;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondCity.class, GunnerConscript.class, SylvanAwakening.class, DoublingSeason.class, Wasteland.class})
class DiamondCityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());

        assertThat(city.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tapsForColorlessMana() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new DiamondCity());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isOne();
        assertThat(city.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Moves its shield counter to any target creature after two creatures enter")
    void movesShieldCounterAfterTwoCreaturesEnter() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(city.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(target.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    @DisplayName("Cannot move a shield counter before two creatures enter")
    void cannotActivateBeforeTwoCreaturesEnter() {
        harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's entering creatures do not satisfy the activation restriction")
    void opponentsCreaturesDoNotEnableActivation() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player2, new GunnerConscript());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(city.isTapped()).isFalse();
        assertThat(city.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    @DisplayName("Can activate without a shield counter, but no counter is moved")
    void canActivateWithoutShieldCounter() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new DiamondCity());
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(city.isTapped()).isTrue();
        assertThat(city.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    @DisplayName("Moves only one shield counter when the land has several")
    void movesOnlyOneShieldCounter() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        city.setCounterCount(CounterType.SHIELD, 3);
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(city.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.SHIELD)).isOne();
        assertThat(city.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No counter moves if Diamond City leaves before resolution")
    void sourceMustRemainOnBattlefield() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(city);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    @DisplayName("A shield counter saves Diamond City from one destruction effect")
    @CardUsed({DiamondCity.class, Wasteland.class})
    void shieldCounterPreventsDestructionOnce() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        harness.addToBattlefield(player1, new Wasteland());

        harness.activateAbility(player1, 1, 1, null, city.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Diamond City");
        assertThat(city.getCounterCount(CounterType.SHIELD)).isZero();
        harness.addToBattlefield(player1, new Wasteland());
        harness.activateAbility(player1, 1, 1, null, city.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Diamond City");
        harness.assertInGraveyard(player1, "Diamond City");
    }

    @Test
    @DisplayName("Moving a shield counter onto the same animated land does nothing even with Doubling Season")
    @CardUsed({DiamondCity.class, GunnerConscript.class, SylvanAwakening.class, DoublingSeason.class})
    void cannotMoveShieldCounterOntoItself() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());
        harness.enterBattlefieldAndReturn(player1, new GunnerConscript());
        harness.castFromHand(player1, new SylvanAwakening(), "{2}{G}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, city.getId());
        harness.passBothPriorities();

        assertThat(city.getCounterCount(CounterType.SHIELD)).isOne();
        assertThat(city.isTapped()).isTrue();
    }
}
