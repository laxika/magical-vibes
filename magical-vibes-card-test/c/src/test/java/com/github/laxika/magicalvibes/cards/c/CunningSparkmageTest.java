package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningSparkmage.class, ArborElf.class, GnarlidPack.class, JaceTheMindSculptor.class})
class CunningSparkmageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent sparkmage = addReadySparkmage(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sparkmage.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addReadySparkmage(player1);
        harness.addToBattlefield(player2, new ArborElf());

        UUID targetId = harness.getPermanentId(player2, "Arbor Elf");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Arbor Elf");
        harness.assertInGraveyard(player2, "Arbor Elf");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addReadySparkmage(player1);
        harness.addToBattlefield(player2, new GnarlidPack());

        UUID targetId = harness.getPermanentId(player2, "Gnarlid Pack");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gnarlid Pack");
    }

    @Test
    @DisplayName("Haste allows activation on the turn it enters")
    void hasteAllowsActivationSameTurn() {
        harness.setHand(player1, List.of(new CunningSparkmage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player2, new GnarlidPack());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        int sparkmageIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Cunning Sparkmage"));
        UUID targetId = harness.getPermanentId(player2, "Gnarlid Pack");
        harness.activateAbility(player1, sparkmageIndex, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gnarlid Pack");
        Permanent target = findPermanent(player2, "Gnarlid Pack");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent sparkmage = addReadySparkmage(player1);
        sparkmage.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can target its own controller")
    void damagesController() {
        addReadySparkmage(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can target itself and dies to its own damage")
    void damagesItself() {
        Permanent sparkmage = addReadySparkmage(player1);

        harness.activateAbility(player1, 0, null, sparkmage.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cunning Sparkmage");
        harness.assertInGraveyard(player1, "Cunning Sparkmage");
    }

    @Test
    @DisplayName("Damage removes a loyalty counter from a planeswalker")
    void damagesPlaneswalker() {
        addReadySparkmage(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent sparkmage = addReadySparkmage(player1);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sparkmage);
        gd.playerGraveyards.get(player1.getId()).add(sparkmage.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not damage a creature that left and returned")
    void doesNotDamageNewPermanent() {
        Permanent sparkmage = addReadySparkmage(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());

        harness.passBothPriorities();

        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(sparkmage.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySparkmage(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CunningSparkmage());
        permanent.setSummoningSick(false);
        return permanent;
    }

}
