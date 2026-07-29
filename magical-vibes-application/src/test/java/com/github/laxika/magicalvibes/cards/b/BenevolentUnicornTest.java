package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BenevolentUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Shock deals 1 instead of 2 to a player")
    void reducesSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Reduces spell damage dealt by its own controller's spells too")
    void reducesOwnControllersSpellDamage() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Shock marks only 1 damage on a creature")
    void reducesSpellDamageToCreature() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A 1-damage spell is reduced to 0 and the creature survives")
    void oneDamageSpellIsFullyReduced() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elves);
        assertThat(elves.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Activated ability damage is not reduced")
    void doesNotReduceAbilityDamage() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        Permanent tim = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        tim.setSummoningSick(false);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(tim),
                null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }
}
