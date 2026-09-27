package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalamaxTheStormsire.class, LightningBolt.class})
class KalamaxTheStormsireTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the first instant each turn while Kalamax is tapped and gets a counter")
    void copiesFirstInstantAndGetsCounter() {
        Permanent kalamax = addKalamax(true);
        harness.setHand(player1, List.of(lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copies only the first instant each turn")
    void copiesOnlyFirstInstantEachTurn() {
        Permanent kalamax = addKalamax(true);
        harness.setHand(player1, List.of(lifeGainInstant(), lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not copy an instant while Kalamax is untapped")
    void doesNotCopyWhileUntapped() {
        Permanent kalamax = addKalamax(false);
        harness.setHand(player1, List.of(lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addKalamax(boolean tapped) {
        Permanent kalamax = harness.addToBattlefieldAndReturn(player1, new KalamaxTheStormsire());
        if (tapped) {
            kalamax.tap();
        } else {
            kalamax.enterUntapped();
        }
        return kalamax;
    }

    private static Card lifeGainInstant() {
        Card card = new Card();
        card.setName("Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
