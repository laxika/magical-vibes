package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdaptiveGemguard;
import com.github.laxika.magicalvibes.cards.h.HiddenCourtyard;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatColony.class, Plains.class, AdaptiveGemguard.class, HiddenCourtyard.class})
class BatColonyTest extends BaseCardTest {

    @Test
    void createsOneBatForEachCaveManaSpentToCastIt() {
        harness.addToBattlefield(player1, new HiddenCourtyard());
        harness.addToBattlefield(player1, new HiddenCourtyard());
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new BatColony()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bat")).isEqualTo(2);
    }

    @Test
    void createsNoBatsWhenNoCaveManaWasSpent() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new BatColony()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bat")).isZero();
    }

    @Test
    void putsACounterOnTargetCreatureWhenACaveEnters() {
        harness.addToBattlefield(player1, new BatColony());
        Permanent bears = addCreatureReady(player1, new AdaptiveGemguard());
        harness.setHand(player1, List.of(new HiddenCourtyard()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForANonCaveLand() {
        harness.addToBattlefield(player1, new BatColony());
        Permanent bears = addCreatureReady(player1, new AdaptiveGemguard());
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerForAnOpponentsCave() {
        harness.addToBattlefield(player1, new BatColony());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AdaptiveGemguard());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HiddenCourtyard()));

        harness.playLand(player2, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void caveEntryWithOnlyAnOpposingCreatureDoesNotRequireATarget() {
        harness.addToBattlefield(player1, new BatColony());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdaptiveGemguard());
        harness.setHand(player1, List.of(new HiddenCourtyard()));

        harness.playLand(player1, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void createsThreeBatsWhenAllThreeManaComeFromRealCaves() {
        harness.addToBattlefield(player1, new HiddenCourtyard());
        harness.addToBattlefield(player1, new HiddenCourtyard());
        harness.addToBattlefield(player1, new HiddenCourtyard());
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        harness.setHand(player1, List.of(new BatColony()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bat")).isEqualTo(3);
        assertThat(countPermanents(player2, "Bat")).isZero();
    }


}
