package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MercurialGeists.class, GrizzlyBears.class, LavaSpike.class, Shock.class})
class MercurialGeistsTest extends BaseCardTest {

    private Permanent addGeists(Player player) {
        Permanent geists = harness.addToBattlefieldAndReturn(player, new MercurialGeists());
        geists.setSummoningSick(false);
        return geists;
    }

    @Test
    void castingInstantOrSorceryBoostsSelf() {
        Permanent geists = addGeists(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new LavaSpike()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(geists.getPowerModifier()).isEqualTo(3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(geists.getPowerModifier()).isEqualTo(6);
    }

    @Test
    void castingCreatureDoesNotTrigger() {
        Permanent geists = addGeists(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(geists.getPowerModifier()).isEqualTo(0);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent geists = addGeists(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(geists.getPowerModifier()).isEqualTo(3);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(geists.getPowerModifier()).isEqualTo(0);
    }

    @Test
    void opponentCastingInstantDoesNotBoostGeists() {
        Permanent geists = addGeists(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(geists.getPowerModifier()).isZero();
        assertThat(geists.getToughnessModifier()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    void boostResolvesBeforeSpellAndDoesNotIncreaseToughness() {
        Permanent geists = addGeists(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        assertThat(geists.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(geists.getPowerModifier()).isEqualTo(3);
        assertThat(geists.getToughnessModifier()).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void eachControlledGeistsBoostsOnlyItself() {
        Permanent first = addGeists(player1);
        Permanent second = addGeists(player1);
        Permanent opposing = addGeists(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(3);
        assertThat(second.getPowerModifier()).isEqualTo(3);
        assertThat(opposing.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }
}
