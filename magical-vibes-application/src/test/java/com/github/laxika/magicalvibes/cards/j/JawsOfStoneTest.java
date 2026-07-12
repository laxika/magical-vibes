package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JawsOfStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Divides Mountain-count damage among a creature and a player")
    void dividesDamageAmongCreatureAndPlayer() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JawsOfStone()));
        harness.addMana(player1, ManaColor.RED, 6);

        // 3 Mountains -> X = 3: 2 to the 2/2 (lethal), 1 to the opponent.
        harness.castInstant(player1, 0, Map.of(bears.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can assign all damage to a single target")
    void dealsAllDamageToOnePlayer() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new JawsOfStone()));
        harness.addMana(player1, ManaColor.RED, 6);

        // 4 Mountains -> X = 4, all to the opponent.
        harness.castInstant(player1, 0, Map.of(player2.getId(), 4));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Counts only the caster's Mountains, not the opponent's")
    void countsOnlyControllersMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new JawsOfStone()));
        harness.addMana(player1, ManaColor.RED, 6);

        // Only player1's 2 Mountains count, so X = 2 (not 5).
        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Assignments must sum to the number of Mountains controlled")
    void assignmentsMustSumToMountainCount() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new JawsOfStone()));
        harness.addMana(player1, ManaColor.RED, 6);

        // 2 Mountains -> X = 2, but 3 is assigned.
        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Total is locked at cast time, not recomputed at resolution")
    void locksMountainCountAtCastTime() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new JawsOfStone()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 3));

        // Sacrifice/remove all Mountains after the spell is on the stack.
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Mountain"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Damage was fixed at cast time, so the opponent still takes 3.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
