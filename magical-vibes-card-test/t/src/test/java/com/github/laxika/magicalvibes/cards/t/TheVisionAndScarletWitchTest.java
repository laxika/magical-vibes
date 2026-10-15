package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheVisionAndScarletWitch.class, GrizzlyBears.class})
class TheVisionAndScarletWitchTest extends BaseCardTest {

    @Test
    void castingASpellAddsRedManaAndAPlusOnePlusOneCounter() {
        Permanent visionAndScarletWitch = addCreatureReady(player1, new TheVisionAndScarletWitch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        ManaPool manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(visionAndScarletWitch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(visionAndScarletWitch.getEffectivePower()).isEqualTo(4);
        assertThat(visionAndScarletWitch.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void castingItselfDoesNotTriggerItsAbility() {
        harness.setHand(player1, List.of(new TheVisionAndScarletWitch()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void opponentsSpellDoesNotTriggerItsAbility() {
        Permanent visionAndScarletWitch = addCreatureReady(player1, new TheVisionAndScarletWitch());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TheVisionAndScarletWitch()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(visionAndScarletWitch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castTriggerUsesTheStackAndResolvesBeforeTheSpell() {
        Permanent visionAndScarletWitch = addCreatureReady(player1, new TheVisionAndScarletWitch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(visionAndScarletWitch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(visionAndScarletWitch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
