package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RollingThunder.class, MoggConscripts.class})
class RollingThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage divided between a creature and a player")
    void dividesDamageAmongCreatureAndPlayer() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player2, new MoggConscripts());
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 10);

        // X = 3 -> 2 to the 2/2 (lethal), 1 to the opponent.
        harness.castSorceryForX(player1, 0, 3, Map.of(conscripts.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Conscripts");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("All X damage may go to a single target")
    void dealsAllDamageToOnePlayer() {
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 10);

        harness.castSorceryForX(player1, 0, 4, Map.of(player2.getId(), 4));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("With X = 0, can choose no targets and deals no damage")
    void canChooseNoTargetsWhenXIsZero() {
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Rolling Thunder");
    }

    @Test
    @DisplayName("With positive X, must choose at least one target")
    void positiveXRequiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorceryForX(player1, 0, 1, Map.of())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each chosen target must receive at least one damage")
    void eachTargetMustReceivePositiveDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MoggConscripts());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MoggConscripts());
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() ->
                harness.castSorceryForX(player1, 0, 3, Map.of(first.getId(), 0, second.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Keeps the original division when one target leaves before resolution")
    void keepsOriginalDivisionWhenTargetLeavesBeforeResolution() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player2, new MoggConscripts());
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorceryForX(player1, 0, 3, Map.of(conscripts.getId(), 2, player2.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).remove(conscripts);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Assignments must sum to X")
    void assignmentsMustSumToX() {
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 10);

        assertThatThrownBy(() ->
                harness.castSorceryForX(player1, 0, 3, Map.of(player2.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }
}
