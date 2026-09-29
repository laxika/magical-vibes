package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyanVengefulSamurai.class, GrizzlyBears.class, Reminisce.class, Shock.class})
class CyanVengefulSamuraiTest extends BaseCardTest {

    @Test
    void costsOneLessForEachCreatureCardInYourGraveyard() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new CyanVengefulSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void putsOneCounterWhenMultipleCreatureCardsLeaveYourGraveyardTogether() {
        Permanent cyan = harness.addToBattlefieldAndReturn(player1, new CyanVengefulSamurai());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cyan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
