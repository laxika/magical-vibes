package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RasputinTheOneiromancer.class)
class RasputinTheOneiromancerTest extends BaseCardTest {

    @Test
    void entersWithOneDreamCounterAndGivesEachOpponentAGoblin() {
        harness.setHand(player1, List.of(new RasputinTheOneiromancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rasputin = findPermanent(player1, "Rasputin, the Oneiromancer");
        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);

        List<Permanent> goblins = findPermanents(player2, "Goblin");
        assertThat(goblins).hasSize(1);
        assertThat(goblins.getFirst().getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(goblins.getFirst().getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    void removesAnyNumberOfDreamCountersForThatMuchColorlessMana() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 3);

        harness.activateAbility(player1, 0, 0, 2, null);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void removesADreamCounterToCreateAProtectedKnight() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.RED)).isTrue();
    }

    private Permanent addReadyRasputin() {
        return addCreatureReady(player1, new RasputinTheOneiromancer());
    }
}
