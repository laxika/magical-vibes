package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DirgurNemesis.class)
class DirgurNemesisTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new DirgurNemesis()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dirgur = findPermanent(player1, "Dirgur Nemesis");
        assertThat(dirgur.isFaceDown()).isTrue();
        assertThat(dirgur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dirgur));

        assertThat(dirgur.isFaceDown()).isFalse();
        assertThat(dirgur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
