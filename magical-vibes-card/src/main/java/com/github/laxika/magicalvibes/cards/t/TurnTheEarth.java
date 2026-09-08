package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "MID", collectorNumber = "205")
public class TurnTheEarth extends Card {

    public TurnTheEarth() {
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 3)
                .addEffect(EffectSlot.SPELL,
                        new ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect(null, 3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        addCastingOption(new FlashbackCast("{1}{G}"));
    }
}
