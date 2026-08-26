package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngryRabble.class, AirElemental.class, GrizzlyBears.class})
class AngryRabbleTest extends BaseCardTest {

    @Test
    void highManaValueSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    void spellWithManaValueLessThanFourDoesNotDealDamage() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void activatedAbilityPutsTwoPlusOnePlusOneCountersOnAngryRabble() {
        Permanent rabble = addCreatureReady(player1, new AngryRabble());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rabble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
