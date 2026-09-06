package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunstarLightsmith.class, LightningBolt.class})
class SunstarLightsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on itself and draws a card for your second spell each turn")
    void secondSpellAddsCounterAndDrawsCard() {
        Permanent lightsmith = addCreatureReady(player1, new SunstarLightsmith());
        Card drawnCard = new LightningBolt();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Does not trigger for your third spell")
    void doesNotTriggerForThirdSpell() {
        Permanent lightsmith = addCreatureReady(player1, new SunstarLightsmith());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
