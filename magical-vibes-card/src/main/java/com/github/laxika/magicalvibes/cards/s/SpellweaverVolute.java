package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.SpellweaverVoluteEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellweaverVoluteTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "FUT", collectorNumber = "59")
public class SpellweaverVolute extends Card {

    public SpellweaverVolute() {
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.INSTANT), GraveyardSearchScope.ALL_GRAVEYARDS));
        addEffect(EffectSlot.SPELL, new SpellweaverVoluteEnterEffect());
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellweaverVoluteTriggerEffect());
    }
}
