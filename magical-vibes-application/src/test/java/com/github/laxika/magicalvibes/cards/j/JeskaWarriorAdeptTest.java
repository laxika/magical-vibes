package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DwarvenDriller;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskaWarriorAdept.class, SuntailHawk.class, DwarvenDriller.class, ChandraNalaar.class})
class JeskaWarriorAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent jeska = addCreatureReady(player1, new JeskaWarriorAdept());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(jeska.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to target creature")
    void deals1DamageToCreature() {
        addCreatureReady(player1, new JeskaWarriorAdept());
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Suntail Hawk"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("One damage does not destroy a 2/2 creature")
    void oneDamageDoesNotDestroyTwoToughnessCreature() {
        addCreatureReady(player1, new JeskaWarriorAdept());
        harness.addToBattlefield(player2, new DwarvenDriller());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Dwarven Driller"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dwarven Driller");
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        addCreatureReady(player1, new JeskaWarriorAdept());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Haste allows activating the tap ability immediately")
    void hasteAllowsImmediateActivation() {
        harness.addToBattlefield(player1, new JeskaWarriorAdept());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the tap ability when Jeska is already tapped")
    void cannotActivateWhenTapped() {
        Permanent jeska = addCreatureReady(player1, new JeskaWarriorAdept());
        jeska.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

}
