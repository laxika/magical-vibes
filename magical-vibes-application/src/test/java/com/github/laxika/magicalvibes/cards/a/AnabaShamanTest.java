package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnabaShaman.class, DwarvenTrader.class})
class AnabaShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AnabaShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Ability can deal 1 damage to its controller")
    void deals1DamageToItsController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new AnabaShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Ability deals 1 damage to target creature, killing a 1/1")
    void deals1DamageKilling1Toughness() {
        addCreatureReady(player1, new AnabaShaman());
        harness.addToBattlefield(player2, new DwarvenTrader());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Dwarven Trader");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dwarven Trader");
        harness.assertInGraveyard(player2, "Dwarven Trader");
    }

    @Test
    @DisplayName("Ability deals 1 damage to target creature, 2/2 survives")
    void deals1DamageDoesNotKill2Toughness() {
        addCreatureReady(player1, new AnabaShaman());
        harness.addToBattlefield(player2, new AnabaShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Anaba Shaman");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Anaba Shaman");
    }

    @Test
    @DisplayName("Activating taps the creature, spends the mana, and puts the ability on the stack")
    void activatingTapsAndPutsOnStack() {
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without the {R} mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new AnabaShaman());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AnabaShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        shaman.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

}
