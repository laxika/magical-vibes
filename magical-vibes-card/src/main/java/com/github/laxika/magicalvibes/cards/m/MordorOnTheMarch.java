package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "512")
@CardRegistration(set = "LTC", collectorNumber = "556")
public class MordorOnTheMarch extends Card {

    public MordorOnTheMarch() {
        addEffect(EffectSlot.SPELL, new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                new CardTypePredicate(CardType.CREATURE),
                true,
                List.of(),
                true,
                true));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
