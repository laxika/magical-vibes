package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.l.LukkaCoppercoatOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeaponizeTheMonsters.class, GrizzlyBears.class, AlmightyBrushwagg.class,
        LukkaCoppercoatOutcast.class})
class WeaponizeTheMonstersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 2 damage to a player")
    void dealsTwoDamageToPlayer() {
        harness.addToBattlefield(player1, new WeaponizeTheMonsters());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices a creature and deals 2 damage to a creature")
    void dealsTwoDamageToCreature() {
        harness.addToBattlefield(player1, new WeaponizeTheMonsters());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new WeaponizeTheMonsters());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificesBeforeDamageResolves() {
        harness.addToBattlefield(player1, new WeaponizeTheMonsters());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Almighty Brushwagg");
        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void canSacrificeTheTargetedCreature() {
        harness.addToBattlefield(player1, new WeaponizeTheMonsters());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Almighty Brushwagg"));
        harness.assertInGraveyard(player1, "Almighty Brushwagg");

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Weaponize the Monsters");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsTwoDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new WeaponizeTheMonsters());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        var lukka = harness.addToBattlefieldAndReturn(player2, new LukkaCoppercoatOutcast());
        lukka.setCounterCount(CounterType.LOYALTY, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Lukka, Coppercoat Outcast"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Almighty Brushwagg");
        harness.assertOnBattlefield(player2, "Lukka, Coppercoat Outcast");
        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        harness.addToBattlefield(player1, new WeaponizeTheMonsters());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertNotInGraveyard(player1, "Almighty Brushwagg");
        assertThat(gd.stack).isEmpty();
    }
}
