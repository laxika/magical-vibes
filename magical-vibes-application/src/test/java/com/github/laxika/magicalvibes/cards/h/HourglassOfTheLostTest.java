package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HourglassOfTheLost.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class,
        MindStone.class, Plains.class})
class HourglassOfTheLostTest extends BaseCardTest {

    @Test
    void tappingAddsWhiteManaAndATimeCounter() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(manaBefore + 1);
        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void returnsEachNonlandPermanentWithTheChosenManaValue() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        hourglass.setCounterCount(CounterType.TIME, 3);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new HillGiant(), new Plains(), new LightningBolt()));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Mind Stone")).hasSize(1);
        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Hill Giant", "Plains", "Lightning Bolt");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Hourglass of the Lost"));
    }
}
