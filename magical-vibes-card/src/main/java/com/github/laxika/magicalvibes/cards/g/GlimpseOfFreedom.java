package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "50")
public class GlimpseOfFreedom extends Card {

    public GlimpseOfFreedom() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addCastingOption(new GraveyardCast(null, "{2}{U}",
                List.of(new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)),
                null, false, true));
    }
}
