package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "12")
public class EsperSentinel extends Card {

    public EsperSentinel() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, SpellCastTriggerEffect.nth(
                1,
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new DrawCardUnlessPaysEffect(1, new SourcePower()))
        ));
    }
}
