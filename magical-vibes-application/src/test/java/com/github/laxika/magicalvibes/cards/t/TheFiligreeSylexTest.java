package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BladedAmbassador;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFiligreeSylex.class, GrizzlyBears.class, LlanowarElves.class, Plains.class, BladedAmbassador.class})
class TheFiligreeSylexTest extends BaseCardTest {

    @Test
    void putsAnOilCounterOnItself() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sylex.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void destroysNonlandsWithMatchingManaValueAcrossTheBattlefield() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        Permanent player1Bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        sylex.setCounterCount(CounterType.OIL, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Bear.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Bear.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elf);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        harness.assertInGraveyard(player1, "The Filigree Sylex");
    }

    @Test
    void removesOilCountersFromTheSylexBeforeSacrificingItToDealTenDamage() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        sylex.setCounterCount(CounterType.OIL, 10);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(sylex.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInGraveyard(player1, "The Filigree Sylex");
    }

    @Test
    void cannotActivateDamageAbilityWithoutTenOilCounters() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        sylex.setCounterCount(CounterType.OIL, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroOilCountersDestroyFaceDownCreaturesButSpareLands() {
        harness.addToBattlefield(player1, new TheFiligreeSylex());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new BladedAmbassador());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "The Filigree Sylex");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(faceDown).contains(land);
        harness.assertInGraveyard(player2, "Bladed Ambassador");
    }

    @Test
    void twoOilCountersSpareFaceDownCreaturesWhoseFrontFaceCostsTwo() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        sylex.setCounterCount(CounterType.OIL, 2);
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new BladedAmbassador());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(faceDown);
        harness.assertNotInGraveyard(player2, "Bladed Ambassador");
    }

    @Test
    void removesTenOilCountersDistributedAcrossControlledPermanents() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        Permanent ambassador = harness.addToBattlefieldAndReturn(player1, new BladedAmbassador());
        sylex.setCounterCount(CounterType.OIL, 4);
        ambassador.setCounterCount(CounterType.OIL, 6);

        harness.activateAbility(player1, 0, 2, null, player2.getId());

        assertThat(sylex.getCounterCount(CounterType.OIL)).isZero();
        assertThat(ambassador.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInGraveyard(player1, "The Filigree Sylex");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ambassador);
    }

    @Test
    void cannotUseOpponentsOilCountersOrOtherCounterTypesToPay() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new BladedAmbassador());
        sylex.setCounterCount(CounterType.OIL, 9);
        sylex.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentPermanent.setCounterCount(CounterType.OIL, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sylex.getCounterCount(CounterType.OIL)).isEqualTo(9);
        assertThat(sylex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentPermanent.getCounterCount(CounterType.OIL)).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sylex);
    }

    @Test
    void damageAbilityCanTargetACreatureAndRemovesExactlyTenCounters() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new TheFiligreeSylex());
        Permanent ambassador = harness.addToBattlefieldAndReturn(player1, new BladedAmbassador());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BladedAmbassador());
        ambassador.setCounterCount(CounterType.OIL, 12);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        assertThat(ambassador.getCounterCount(CounterType.OIL)).isEqualTo(2);
        harness.assertInGraveyard(player1, "The Filigree Sylex");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Bladed Ambassador");
        harness.assertLife(player2, 20);
    }

    @Test
    void destructionUsesSacrificedSourcesCountersWhenTheSameCardReturnsBeforeResolution() {
        TheFiligreeSylex card = new TheFiligreeSylex();
        Permanent source = harness.addToBattlefieldAndReturn(player1, card);
        Permanent ambassador = harness.addToBattlefieldAndReturn(player2, new BladedAmbassador());
        source.setCounterCount(CounterType.OIL, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "The Filigree Sylex");

        harness.setGraveyard(player1, List.of());
        Permanent returnedSylex = harness.addToBattlefieldAndReturn(player1, card);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ambassador);
        harness.assertInGraveyard(player2, "Bladed Ambassador");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returnedSylex);
        harness.assertInGraveyard(player1, "The Filigree Sylex");
    }
}
