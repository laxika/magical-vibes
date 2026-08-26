package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingLizards.class, AirElemental.class, GrizzlyBears.class})
class LurkingLizardsTest extends BaseCardTest {

    @Test
    void castingSpellWithManaValueFourOrGreaterPutsCounterOnLurkingLizards() {
        harness.addToBattlefield(player1, new LurkingLizards());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent lizards = findPermanent(player1, "Lurking Lizards");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingSpellWithManaValueLessThanFourDoesNotPutCounterOnLurkingLizards() {
        harness.addToBattlefield(player1, new LurkingLizards());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent lizards = findPermanent(player1, "Lurking Lizards");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentCastingSpellDoesNotPutCounterOnLurkingLizards() {
        harness.addToBattlefield(player1, new LurkingLizards());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        Permanent lizards = findPermanent(player1, "Lurking Lizards");
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
