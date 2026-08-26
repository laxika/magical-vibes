package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StaffOfNin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RemKarolusStalwartSlayer.class, Shock.class, GrizzlyBears.class, SerraAngel.class, StaffOfNin.class})
class RemKarolusStalwartSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents spell damage to its controller")
    void preventsSpellDamageToController() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents spell damage to another permanent it protects")
    void preventsSpellDamageToControlledPermanent() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Adds one damage to spells that damage an opponent")
    void addsDamageToSpellDamageAgainstOpponent() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Adds one damage to spells that damage an opponent's permanent")
    void addsDamageToSpellDamageAgainstOpponentsPermanent() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not affect damage from abilities")
    void doesNotAffectAbilityDamage() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfNin());
        staff.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
