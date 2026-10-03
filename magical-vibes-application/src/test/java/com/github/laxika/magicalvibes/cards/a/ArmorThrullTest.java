package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConchHorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmorThrull.class, ConchHorn.class})
class ArmorThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Armor Thrull puts a +1/+2 counter on target creature")
    void sacrificesAndPlacesCounter() {
        Permanent armorThrull = addCreatureReady(player1, new ArmorThrull());
        Permanent target = addCreatureReady(player1, new ArmorThrull());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(armorThrull).isNotIn(harness.getGameData().playerBattlefields.get(player1.getId()));
        harness.assertInGraveyard(player1, "Armor Thrull");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can put a counter on an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent armorThrull = addCreatureReady(player1, new ArmorThrull());
        Permanent target = addCreatureReady(player2, new ArmorThrull());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(armorThrull).isNotIn(harness.getGameData().playerBattlefields.get(player1.getId()));
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new ArmorThrull());
        harness.addToBattlefield(player1, new ConchHorn());
        UUID targetId = harness.getPermanentId(player1, "Conch Horn");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Tap and sacrifice costs are paid before the counter ability resolves")
    void paysCostsBeforeResolution() {
        Permanent source = addCreatureReady(player1, new ArmorThrull());
        Permanent target = addCreatureReady(player2, new ArmorThrull());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Armor Thrull");
        harness.assertInGraveyard(player1, "Armor Thrull");
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Armor Thrull can target itself but is sacrificed before resolution")
    void canTargetItself() {
        Permanent source = addCreatureReady(player1, new ArmorThrull());

        harness.activateAbility(player1, 0, null, source.getId());

        harness.assertNotOnBattlefield(player1, "Armor Thrull");
        harness.assertInGraveyard(player1, "Armor Thrull");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Armor Thrull cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent source = addCreatureReady(player1, new ArmorThrull());
        source.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new ArmorThrull());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Armor Thrull");
        harness.assertNotInGraveyard(player1, "Armor Thrull");
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Armor Thrull cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new ArmorThrull());
        source.tap();
        Permanent target = addCreatureReady(player2, new ArmorThrull());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Armor Thrull");
        harness.assertNotInGraveyard(player1, "Armor Thrull");
        assertThat(gd.stack).isEmpty();
    }
}
