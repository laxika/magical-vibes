package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BurnDownTheHouse;
import com.github.laxika.magicalvibes.cards.d.DuelForDominance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LambholtHarrier;
import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SludgeMonster;
import com.github.laxika.magicalvibes.cards.s.StaffOfNin;
import com.github.laxika.magicalvibes.cards.s.StormriderSpirit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RemKarolusStalwartSlayer.class, Shock.class, GrizzlyBears.class, SerraAngel.class,
        StaffOfNin.class, BurnDownTheHouse.class, DuelForDominance.class, LambholtHarrier.class,
        PlayWithFire.class, SludgeMonster.class, StormriderSpirit.class})
class RemKarolusStalwartSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents spell damage to its controller")
    void preventsSpellDamageToController() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

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

        harness.castAndResolveInstant(player2, 0, bear.getId());

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

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Adds one damage to spells that damage an opponent's permanent")
    void addsDamageToSpellDamageAgainstOpponentsPermanent() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

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

    @Test
    @DisplayName("Rem does not prevent spell damage to itself")
    void doesNotPreventSpellDamageToItself() {
        Permanent rem = harness.addToBattlefieldAndReturn(player1, new RemKarolusStalwartSlayer());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, rem.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rem);
        assertThat(rem.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A damage sweeper kills Rem while its other creatures are protected")
    void sweeperKillsRemButProtectsOtherCreatures() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        Permanent harrier = harness.addToBattlefieldAndReturn(player1, new LambholtHarrier());
        harness.addToBattlefield(player2, new StormriderSpirit());
        harness.setHand(player1, List.of(new BurnDownTheHouse()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Rem Karolus, Stalwart Slayer");
        harness.assertNotOnBattlefield(player1, "Rem Karolus, Stalwart Slayer");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(harrier);
        assertThat(harrier.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Stormrider Spirit");
    }

    @Test
    @DisplayName("A fight spell's creature damage is neither prevented nor increased")
    void doesNotModifyFightDamage() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        Permanent harrier = harness.addToBattlefieldAndReturn(player1, new LambholtHarrier());
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new StormriderSpirit());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(harrier.getId(), spirit.getId()));

        harness.assertInGraveyard(player1, "Lambholt Harrier");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spirit);
        assertThat(spirit.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rem's damage bonus stops when Sludge Monster removes its abilities")
    void damageBonusStopsWhenAbilitiesAreRemoved() {
        Permanent rem = harness.addToBattlefieldAndReturn(player1, new RemKarolusStalwartSlayer());
        harness.addToBattlefield(player2, new SludgeMonster());
        rem.setCounterCount(CounterType.SLIME, 1);
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new StormriderSpirit());
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, spirit.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spirit);
        assertThat(spirit.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's spell also receives the bonus against their own permanent")
    void increasesOpponentsSpellDamageToTheirOwnPermanent() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new StormriderSpirit());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, spirit.getId());

        harness.assertInGraveyard(player2, "Stormrider Spirit");
        harness.assertNotOnBattlefield(player2, "Stormrider Spirit");
    }

    @Test
    @DisplayName("Does not prevent damage to its controller from an activated ability")
    void doesNotPreventAbilityDamageToController() {
        harness.addToBattlefield(player1, new RemKarolusStalwartSlayer());
        harness.addToBattlefield(player2, new StaffOfNin());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }
}
