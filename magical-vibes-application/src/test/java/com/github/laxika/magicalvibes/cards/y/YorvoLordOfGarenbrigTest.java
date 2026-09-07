package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YorvoLordOfGarenbrig.class, GrizzlyBears.class, ColossalDreadmaw.class,
        WoollyThoctar.class, HillGiant.class})
class YorvoLordOfGarenbrigTest extends BaseCardTest {

    @Test
    void entersWithFourCountersAndDoesNotTriggerForItself() {
        Permanent yorvo = castYorvo();

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void putsOneCounterWhenAnotherGreenCreatureEnters() {
        Permanent yorvo = castYorvo();

        castCreature(new GrizzlyBears(), ManaColor.GREEN, ManaColor.GREEN);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void putsAnotherCounterWhenEnteringCreatureIsLargerAfterFirstCounter() {
        Permanent yorvo = castYorvo();

        castCreature(new ColossalDreadmaw(), ManaColor.GREEN, ManaColor.GREEN, ManaColor.GREEN,
                ManaColor.GREEN, ManaColor.GREEN, ManaColor.GREEN);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void comparesPowerAfterPuttingTheFirstCounterOnYorvo() {
        Permanent yorvo = castYorvo();

        castCreature(new WoollyThoctar(), ManaColor.RED, ManaColor.GREEN, ManaColor.WHITE);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void ignoresNonGreenCreatures() {
        Permanent yorvo = castYorvo();

        castCreature(new HillGiant(), ManaColor.RED, ManaColor.RED, ManaColor.RED, ManaColor.RED);

        assertThat(yorvo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private Permanent castYorvo() {
        castCreature(new YorvoLordOfGarenbrig(), ManaColor.GREEN, ManaColor.GREEN, ManaColor.GREEN);
        return findPermanent(player1, "Yorvo, Lord of Garenbrig");
    }

    private void castCreature(Card creature, ManaColor... mana) {
        harness.setHand(player1, List.of(creature));
        for (ManaColor color : mana) {
            harness.addMana(player1, color, 1);
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
