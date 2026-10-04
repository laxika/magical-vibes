package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondValley.class, GrizzlyBears.class})
class DiamondValleyTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndGainsItsToughness() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, indexOf(player1, valley), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutCreatureToSacrifice() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, valley), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificesChosenCreatureUsingItsModifiedToughness() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        chosen.tap();
        harness.setLife(player1, 10);

        harness.activateAbility(player1, indexOf(player1, valley), null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other).doesNotContain(chosen);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(valley.isTapped()).isTrue();
    }

    @Test
    void snapshotsModifiedToughnessForAutomaticSacrifice() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, indexOf(player1, valley), null, null);

        harness.assertLife(player1, 10);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(valley.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    void markedDamageDoesNotReduceLifeGained() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setMarkedDamage(1);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, indexOf(player1, valley), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, valley), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(valley.isTapped()).isFalse();
    }

    @Test
    void cannotActivateTappedValley() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());
        valley.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, valley), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
