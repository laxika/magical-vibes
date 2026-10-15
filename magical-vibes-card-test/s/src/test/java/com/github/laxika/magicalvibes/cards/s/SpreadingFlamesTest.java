package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({SpreadingFlames.class, GrizzlyBears.class, HillGiant.class})
class SpreadingFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Divides 6 damage among target creatures")
    void dividesDamageAmongCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, Map.of(
                first.getId(), 2,
                second.getId(), 2,
                third.getId(), 2
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(first.getId()))
                .noneMatch(p -> p.getId().equals(second.getId()))
                .anyMatch(p -> p.getId().equals(third.getId()) && p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Cannot assign damage to a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 6))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage assignments must sum to 6")
    void damageAssignmentsMustSumToSix() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAssignAllDamageToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, Map.of(target.getId(), 6));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Spreading Flames");
    }

    @Test
    void cannotAssignZeroDamageToATarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(first.getId(), 6, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithoutTargets() {
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageFromAnIllegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, Map.of(first.getId(), 4, second.getId(), 2));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerHands.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spreading Flames");
    }

    @Test
    void canAssignOneDamageToEachOfSixCreatures() {
        Map<java.util.UUID, Integer> assignments = new java.util.LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            assignments.put(target.getId(), 1);
        }
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, assignments);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(6)
                .allSatisfy(p -> assertThat(p.getMarkedDamage()).isEqualTo(1));
    }
}
