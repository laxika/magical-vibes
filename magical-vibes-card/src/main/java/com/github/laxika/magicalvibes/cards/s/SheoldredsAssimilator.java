package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfExiledCardIntoTopFiveEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "YONE", collectorNumber = "10")
public class SheoldredsAssimilator extends Card {

    public SheoldredsAssimilator() {
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 1);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, graveyardTrigger());
        addEffect(EffectSlot.ON_ATTACK, graveyardTrigger());
    }

    private CardEffect graveyardTrigger() {
        return new ExileTargetCardFromGraveyardThenEffect(
                null,
                new MayEffect(new ConjureDuplicateOfExiledCardIntoTopFiveEffect(),
                        "Conjure a duplicate of that card into the top five cards of your library?"));
    }
}
