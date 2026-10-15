package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bombard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheRealm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleAltisaur.class, PygmyAllosaurus.class, Shock.class, GrizzlyBears.class,
        Bombard.class, ShieldOfTheRealm.class, TurnToFrog.class})
class TempleAltisaurTest extends BaseCardTest {

    @Test
    void reducesDamageToAnotherDinosaurYouControlToOne() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new PygmyAllosaurus());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, dinosaur.getId());

        assertThat(dinosaur.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotPreventDamageToTempleAltisaurItself() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, temple.getId());

        assertThat(temple.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotPreventDamageToNonDinosaurCreatureYouControl() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void reducesCombatDamageToAnotherDinosaurYouControlToOne() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent dinosaur = addCreatureReady(player1, new PygmyAllosaurus());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        dinosaur.setBlocking(true);
        dinosaur.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(dinosaur.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @CardUsed({TempleAltisaur.class, Bombard.class})
    void doesNotProtectAnOpponentsDinosaur() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player2, new TempleAltisaur());
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, dinosaur.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dinosaur);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(dinosaur.getCard());
    }

    @Test
    @CardUsed({TempleAltisaur.class, Bombard.class})
    void multipleAltisaursProtectEachOtherWithoutReducingDamageBelowOne() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());
        harness.addToBattlefield(player1, new TempleAltisaur());
        harness.setHand(player2, List.of(new Bombard(), new Bombard()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.castAndResolveInstant(player2, 0, second.getId());

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
    }

    @Test
    @CardUsed({TempleAltisaur.class, Bombard.class})
    void eachSeparateDamageEventStillDealsOneDamage() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());
        harness.setHand(player2, List.of(new Bombard(), new Bombard()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castAndResolveInstant(player2, 0, dinosaur.getId());
        harness.castAndResolveInstant(player2, 0, dinosaur.getId());

        assertThat(dinosaur.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dinosaur);
    }

    @Test
    @CardUsed({TempleAltisaur.class, GrizzlyBears.class})
    void simultaneousCombatDamageIsReducedToOneFromEachSource() {
        Permanent attacker = addCreatureReady(player1, new TempleAltisaur());
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 1));

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @CardUsed({TempleAltisaur.class, TurnToFrog.class, Bombard.class})
    void preventionStopsWhenAltisaurLosesItsAbilities() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());
        harness.setHand(player2, List.of(new TurnToFrog(), new Bombard()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.castAndResolveInstant(player2, 0, dinosaur.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dinosaur);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dinosaur.getCard());
    }

    @Test
    @CardUsed({TempleAltisaur.class, ShieldOfTheRealm.class, Bombard.class})
    void anotherPreventionEffectCanPreventTheRemainingOneDamage() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheRealm());
        shield.setAttachedTo(dinosaur.getId());
        harness.setHand(player2, List.of(new Bombard()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, dinosaur.getId());

        assertThat(dinosaur.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dinosaur);
    }
}
