package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

public class TakeATripTo extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("TakeATripTo", new OracleData(
                "Take a Trip to...",
                CardType.SORCERY,
                Set.of(),
                "{4}{U}{R}",
                CardColor.BLUE,
                List.of(CardColor.BLUE, CardColor.RED),
                List.of(CardColor.BLUE, CardColor.RED),
                Set.of(),
                List.of(),
                "Draw two cards. This spell deals 2 damage to each opponent.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public TakeATripTo() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
    }
}
