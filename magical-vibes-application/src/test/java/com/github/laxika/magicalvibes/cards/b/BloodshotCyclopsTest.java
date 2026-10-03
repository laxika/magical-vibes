package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VerduranEnchantress;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodshotCyclops.class, Forest.class, GrizzlyBears.class, VerduranEnchantress.class})
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

    @Test
    @DisplayName("A zero-power sacrifice deals no damage")
    void zeroPowerSacrificeDealsNoDamage() {
        addReadyCyclops(player1);
        Permanent sacrifice = addCreatureReady(player1, new VerduranEnchantress());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Verduran Enchantress");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can sacrifice a tapped, summoning-sick creature")
    void canSacrificeTappedSummoningSickCreature() {
        addReadyCyclops(player1);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setSummoningSick(true);
        sacrifice.setTapped(true);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new BloodshotCyclops());
        cyclops.setSummoningSick(true);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cyclops.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cyclops, bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself while sacrificing another creature")
    void canTargetItself() {
        Permanent cyclops = addReadyCyclops(player1);
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, cyclops.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bloodshot Cyclops");
        assertThat(cyclops.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing the target pays the cost but leaves no legal target")
    void canSacrificeTheTarget() {
        Permanent cyclops = addReadyCyclops(player1);
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(cyclops.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCyclops(Player player) {
        return addCreatureReady(player, new BloodshotCyclops());
    }
}
