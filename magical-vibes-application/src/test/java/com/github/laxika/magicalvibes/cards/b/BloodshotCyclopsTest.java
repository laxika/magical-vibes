package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodshotCyclops.class, Forest.class, GrizzlyBears.class})
class BloodshotCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to sacrificed creature's power to target player")
    void dealsSacrificedPowerToPlayer() {
        addReadyCyclops(player1);
        Permanent sacrificedCreature = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's boosted (effective) power")
    void usesBoostedPower() {
        addReadyCyclops(player1);
        Permanent sacrificedCreature = addCreatureReady(player1, new GrizzlyBears());
        sacrificedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power becomes 3
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals damage equal to sacrificed power to a target creature, killing it")
    void dealsSacrificedPowerToCreature() {
        addReadyCyclops(player1);
        Permanent sacrificedCreature = addCreatureReady(player1, new GrizzlyBears()); // sacrificed, power 2
        Permanent victim = addCreatureReady(player2, new GrizzlyBears()); // 2/2 victim

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Taps the Cyclops and sacrifices the creature as part of the cost")
    void tapsAndSacrificesAsCost() {
        Permanent cyclops = addReadyCyclops(player1);
        Permanent sacrificedCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());

        assertThat(cyclops.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can sacrifice itself, dealing its own power to the target")
    void canSacrificeItself() {
        addReadyCyclops(player1); // 4/4, the only creature available
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Bloodshot Cyclops");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void rejectsNonAnyTargetPermanent() {
        Permanent cyclops = addReadyCyclops(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cyclops.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(cyclops, bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyCyclops(Player player) {
        return addCreatureReady(player, new BloodshotCyclops());
    }
}
