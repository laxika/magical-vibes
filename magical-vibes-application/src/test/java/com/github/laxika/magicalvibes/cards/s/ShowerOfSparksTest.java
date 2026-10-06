package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Acridian;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShowerOfSparks.class, Acridian.class, LilianaVess.class, Mountain.class})
class ShowerOfSparksTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the target creature and target player")
    void damagesCreatureAndPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), player2.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target planeswalker")
    void damagesPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Deals damage to the remaining legal target if the creature target leaves")
    void resolvesWithOneLegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(creature.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Rejects a noncreature as the first target")
    void rejectsNoncreatureFirstTarget() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountain.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals 1 damage to its controller when that player is the second target")
    void damagesItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), player1.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Rejects a non-planeswalker permanent as the second target")
    void rejectsNonPlaneswalkerSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals damage to the creature when the planeswalker target leaves")
    void resolvesWhenPlaneswalkerLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shower of Sparks");
    }

    @Test
    @DisplayName("Does not resolve when both targets leave")
    void doesNotResolveWithNoLegalTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shower of Sparks");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires both targets when cast")
    void rejectsMissingSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal creature damage does not prevent damage to the player")
    void lethalCreatureDamageStillDamagesPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());
        creature.setMarkedDamage(3);
        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), player2.getId()));

        harness.assertInGraveyard(player2, "Acridian");
        harness.assertNotOnBattlefield(player2, "Acridian");
        harness.assertLife(player2, 19);
    }
}
