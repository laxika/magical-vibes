package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AxgardArtisan.class, BondBeetle.class})
class AxgardArtisanTest extends BaseCardTest {

    @Test
    void createsTreasureWhenCounterIsPutOnIt() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new AxgardArtisan());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        castBondBeetle(artisan);

        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void createsOnlyOneTreasurePerTurnAndTriggersAgainOnTheFollowingTurn() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new AxgardArtisan());
        harness.setHand(player1, List.of(new BondBeetle(), new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        castBondBeetle(artisan);
        castBondBeetle(artisan);

        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        castBondBeetle(artisan);

        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    private void castBondBeetle(Permanent target) {
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
