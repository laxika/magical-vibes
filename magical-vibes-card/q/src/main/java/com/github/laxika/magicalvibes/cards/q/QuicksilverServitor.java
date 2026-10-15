package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoHandThisTurnPredicate;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "5")
public class QuicksilverServitor extends Card {

    public QuicksilverServitor() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardPutIntoHandThisTurnPredicate(),
                        List.of(new ProliferateEffect())));
    }
}
