package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DalkovanPackbeasts;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReputableMerchant.class, Forest.class, DalkovanPackbeasts.class})
class ReputableMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you control")
    void etbPutsCounterOnTargetCreatureYouControl() {
        Permanent packbeasts = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());

        harness.setHand(player1, List.of(new ReputableMerchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, 0, packbeasts.getId());
        resolveAllTriggers();

        assertThat(packbeasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger puts a +1/+1 counter on target creature you control")
    void deathPutsCounterOnTargetCreatureYouControl() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new ReputableMerchant());
        Permanent packbeasts = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, merchant));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, packbeasts.getId());
        harness.passBothPriorities();

        assertThat(packbeasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent packbeasts = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());

        harness.setHand(player1, List.of(new ReputableMerchant()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, packbeasts.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new ReputableMerchant()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can put its counter on Reputable Merchant itself")
    void etbCanTargetItself() {
        Permanent merchant = harness.enterBattlefieldAndReturn(player1, new ReputableMerchant());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, merchant.getId());
        resolveAllTriggers();

        assertThat(merchant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not put a counter on a target that left the battlefield")
    void etbTargetLeavesBeforeResolution() {
        Permanent packbeasts = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        harness.setHand(player1, List.of(new ReputableMerchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, 0, packbeasts.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, packbeasts));
        resolveAllTriggers();

        assertThat(packbeasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Reputable Merchant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Dalkovan Packbeasts");
    }

    @Test
    @DisplayName("Death with no remaining creatures does not require an illegal target")
    void deathWithNoLegalTargets() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new ReputableMerchant());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, merchant));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Reputable Merchant");
    }
}
