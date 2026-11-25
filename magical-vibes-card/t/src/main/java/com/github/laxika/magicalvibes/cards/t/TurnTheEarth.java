package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "MID", collectorNumber = "205")
@CardRegistration(set = "DBL", collectorNumber = "205")
public class TurnTheEarth extends Card {

    public TurnTheEarth() {
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 3)
                .addSpellEffect(PutCardsIntoLibraryEffect.shuffledIn(Subjects.targetGraveyardCards(
                        null, GraveyardSearchScope.ALL_GRAVEYARDS, 3)));
        addSpellEffect(new GainLifeEffect(2));
        addCastingOption(new FlashbackCast("{1}{G}"));
    }
}
