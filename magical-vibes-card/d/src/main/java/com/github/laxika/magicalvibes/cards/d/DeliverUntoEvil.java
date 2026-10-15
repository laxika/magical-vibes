package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DeliverUntoEvilEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "WAR", collectorNumber = "85")
public class DeliverUntoEvil extends Card {

    public DeliverUntoEvil() {
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 4);
        addEffect(EffectSlot.SPELL, new DeliverUntoEvilEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
