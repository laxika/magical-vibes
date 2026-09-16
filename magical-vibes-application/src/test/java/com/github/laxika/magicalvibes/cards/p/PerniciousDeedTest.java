package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CoalitionFlag;
import com.github.laxika.magicalvibes.cards.c.Cromat;
import com.github.laxika.magicalvibes.cards.e.EmblazonedGolem;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        PerniciousDeed.class,
        CoalitionFlag.class,
        Cromat.class,
        EmblazonedGolem.class,
        Millstone.class,
        Ornithopter.class,
        PhyrexianArena.class,
        UrborgElf.class,
        YavimayaCoast.class
})
class PerniciousDeedTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys artifacts, creatures, and enchantments with mana value X or less on both battlefields")
    void destroysMatchingPermanentsUpToX() {
        addDeed(2);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new EmblazonedGolem());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new UrborgElf());
        Permanent flag = harness.addToBattlefieldAndReturn(player1, new CoalitionFlag());
        flag.setAttachedTo(elf.getId());
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.addToBattlefield(player1, new Cromat());
        harness.addToBattlefield(player1, new YavimayaCoast());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new EmblazonedGolem());
        harness.addToBattlefield(player2, new UrborgElf());
        harness.addToBattlefield(player2, new PhyrexianArena());
        harness.addToBattlefield(player2, new YavimayaCoast());

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Phyrexian Arena", "Cromat", "Yavimaya Coast");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Phyrexian Arena", "Yavimaya Coast");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player1, "Emblazoned Golem");
        harness.assertInGraveyard(player1, "Urborg Elf");
        harness.assertInGraveyard(player1, "Coalition Flag");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Emblazoned Golem");
        harness.assertInGraveyard(player2, "Urborg Elf");
    }

    @Test
    @DisplayName("X=0 destroys only zero-mana-value matching permanents")
    void zeroOnlyDestroysZeroManaValue() {
        addDeed(0);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new EmblazonedGolem());
        harness.addToBattlefield(player1, new UrborgElf());
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Millstone");
        harness.assertOnBattlefield(player1, "Emblazoned Golem");
        harness.assertOnBattlefield(player1, "Urborg Elf");
        harness.assertOnBattlefield(player1, "Phyrexian Arena");
        harness.assertOnBattlefield(player1, "Yavimaya Coast");
    }

    @Test
    @DisplayName("Pernicious Deed is sacrificed as an activation cost")
    void sacrificesAsCost() {
        addDeed(2);
        harness.addToBattlefield(player2, new UrborgElf());

        harness.activateAbility(player1, 0, 2, null);

        harness.assertNotOnBattlefield(player1, "Pernicious Deed");
        harness.assertInGraveyard(player1, "Pernicious Deed");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Urborg Elf");
    }

    @Test
    @DisplayName("Cannot activate when the announced X value cannot be paid")
    void cannotActivateWithoutEnoughMana() {
        prepareDeed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Pernicious Deed");
        harness.assertNotInGraveyard(player1, "Pernicious Deed");
    }

    @Test
    @DisplayName("Respects regeneration because the ability does not prohibit it")
    void regenerationShieldPreventsDestruction() {
        addDeed(2);
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new UrborgElf());
        protectedCreature.setRegenerationShield(1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Urborg Elf");
        harness.assertNotInGraveyard(player2, "Urborg Elf");
    }

    private void addDeed(int xValue) {
        prepareDeed();
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }

    private void prepareDeed() {
        harness.addToBattlefield(player1, new PerniciousDeed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
