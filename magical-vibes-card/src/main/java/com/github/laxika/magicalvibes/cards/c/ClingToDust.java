package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "87")
public class ClingToDust extends Card {

    public ClingToDust() {
        addEffect(EffectSlot.SPELL,
                ExileGraveyardCardWithConditionalBonusEffect.creatureCardGainsLifeElseDraw(3, 1));
        addCastingOption(new GraveyardCast(null, "{3}{B}",
                List.of(new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)),
                null, false, true));
    }
}
