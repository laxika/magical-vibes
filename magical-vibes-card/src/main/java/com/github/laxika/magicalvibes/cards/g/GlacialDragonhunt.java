package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TDM", collectorNumber = "188")
public class GlacialDragonhunt extends Card {

    public GlacialDragonhunt() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
        addEffect(EffectSlot.SPELL, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new DealDamageToTargetCreatureEffect(3),
                        "a card",
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                "Discard a card?"));
        addCastingOption(new HarmonizeCast("{4}{U}{R}"));
    }
}
