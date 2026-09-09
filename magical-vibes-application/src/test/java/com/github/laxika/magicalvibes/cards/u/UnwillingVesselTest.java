package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnwillingVessel.class, GloriousAnthem.class, DazzlingTheaterPropRoom.class, WrathOfGod.class})
class UnwillingVesselTest extends BaseCardTest {

    @Test
    void putsPossessionCounterOnEnchantmentEntry() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new UnwillingVessel());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vessel.getCounterCount(CounterType.POSSESSION)).isEqualTo(1);
    }

    @Test
    void putsPossessionCounterOnFullyUnlockedRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new UnwillingVessel());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(vessel.getCounterCount(CounterType.POSSESSION)).isEqualTo(1);
    }

    @Test
    void createsSpiritWithPowerAndToughnessEqualToItsCountersWhenItDies() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new UnwillingVessel());
        vessel.setCounterCount(CounterType.POSSESSION, 2);
        vessel.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        assertThat(spirits.getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(spirits.getFirst().getEffectiveToughness()).isEqualTo(3);
        assertThat(spirits.getFirst().getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(spirits.getFirst().getCard().hasKeyword(com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
    }
}
