package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshlingThePilgrim.class, WoodlandChangeling.class, NamelessInversion.class})
class AshlingThePilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("First two resolutions only add +1/+1 counters")
    void firstTwoResolutionsAddCounters() {
        Permanent ashling = addAshling(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 4);

        activateAndResolve();
        activateAndResolve();

        assertThat(ashling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Third resolution removes all counters and deals that much damage to each creature and player")
    void thirdResolutionExplodes() {
        addAshling(player1);
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 6);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        // 3 +1/+1 counters removed → 3 damage to each creature and each player.
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Woodland Changeling"); // 2/2 dies to 3 damage
        harness.assertInGraveyard(player1, "Ashling the Pilgrim"); // 1/1 (counters removed) dies to its own blast
    }

    @Test
    @DisplayName("Bonus fires only on the exact third resolution, not on later ones")
    void bonusOnlyOnThirdResolution() {
        Permanent ashling = addAshling(player1);
        ashling.setToughnessModifier(30); // survive its own blast so we can keep activating
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 12);

        for (int i = 0; i < 6; i++) {
            activateAndResolve();
        }

        // Only the third resolution dealt damage; the 4th–6th just re-accumulate counters.
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(ashling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The blast includes counters already present before the first resolution")
    void blastIncludesExistingCounters() {
        Permanent ashling = addAshling(player1);
        ashling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.addMana(player1, ManaColor.RED, 6);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player1, "Ashling the Pilgrim");
    }

    @Test
    @DisplayName("Pending activations do not count until they resolve")
    void stackedActivationsCountResolutions() {
        Permanent ashling = addAshling(player1);
        harness.addMana(player1, ManaColor.RED, 6);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.passBothPriorities();
        assertThat(ashling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        assertThat(ashling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Ashling the Pilgrim");
    }

    @Test
    @DisplayName("Different Ashlings count their resolutions independently")
    void separatePermanentsHaveSeparateResolutionCounts() {
        Permanent first = addAshling(player1);
        Permanent second = addAshling(player2);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        activateAndResolve();
        activateAndResolve();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Resolution count resets on the next turn while counters remain")
    void resolutionCountResetsEachTurn() {
        Permanent ashling = addAshling(player1);
        harness.addMana(player1, ManaColor.RED, 4);
        activateAndResolve();
        activateAndResolve();

        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        gs.declareAttackers(gd, player1, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 6);
        activateAndResolve();

        assertThat(ashling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        activateAndResolve();
        activateAndResolve();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Ashling the Pilgrim");
    }

    @Test
    @DisplayName("Removing Ashling before the third resolution prevents the blast")
    void absentSourceCannotRemoveCountersOrDealDamage() {
        Permanent ashling = addAshling(player1);
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.RED, 6);
        activateAndResolve();
        activateAndResolve();
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, ashling.getId());
        harness.assertInGraveyard(player1, "Ashling the Pilgrim");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Woodland Changeling");
    }

    private void activateAndResolve() {
        harness.withAutoStop(gd.currentStep, () -> {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        });
    }

    private Permanent addAshling(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AshlingThePilgrim());
        perm.setSummoningSick(false);
        return perm;
    }
}
