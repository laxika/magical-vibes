package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Boulderfall.class, GrizzlyBears.class, BronzeSable.class, VoyagesEnd.class})
class BoulderfallTest extends BaseCardTest {

    @Test
    void dividesDamageAmongCreatureAndPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, Map.of(bears.getId(), 3, player2.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void dealsAllDamageToOnePlayer() {
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 5));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void assignmentsMustSumToFive() {
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachTargetMustReceiveAtLeastOneDamage() {
        Permanent sable = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(sable.getId(), 0, player2.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithoutTargets() {
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDivideDamageBetweenBothPlayers() {
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 2, player2.getId(), 3));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    void canAssignOneDamageToEachOfFiveCreatures() {
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            Permanent sable = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
            assignments.put(sable.getId(), 1);
        }
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, assignments);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bronze Sable");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotRedistributeDamageWhenCreatureTargetLeaves() {
        Permanent sable = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 8);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, Map.of(sable.getId(), 3, player2.getId(), 2));
        harness.castInstant(player2, 0, sable.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Bronze Sable");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Boulderfall");
    }
}
