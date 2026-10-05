package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagicMissile.class, Cancel.class, GrizzlyBears.class, Unsummon.class, Mordenkainen.class})
class MagicMissileTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage divided among one, two, or three targets")
    void dividesDamageAmongThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(
                first.getId(), 1,
                second.getId(), 1,
                player2.getId(), 1
        ));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Rejects more than three targets")
    void rejectsMoreThanThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 0,
                player2.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        MagicMissile missile = new MagicMissile();
        harness.setHand(player1, List.of(missile));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 3));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, missile.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void dividesDamageBetweenControllerAndOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 1, bear.getId(), 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void rejectsZeroDamageAssignedToATarget() {
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsDivisionThatDoesNotTotalThree() {
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageWhenOneTargetBecomesIllegal() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, Map.of(bear.getId(), 2, player2.getId(), 1));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Magic Missile");
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetBecomesIllegal() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, Map.of(bear.getId(), 3));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Magic Missile");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsDamageToAPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new Mordenkainen());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(planeswalker.getId(), 3));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Mordenkainen");
    }
}
