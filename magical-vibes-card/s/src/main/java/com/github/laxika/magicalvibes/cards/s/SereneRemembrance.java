package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "GTC", collectorNumber = "132")
public class SereneRemembrance extends Card {

    public SereneRemembrance() {
        // One instruction: the spell's owner's library, which the targets may share, is shuffled once.
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 3)
                .addSpellEffect(PutCardsIntoLibraryEffect.shuffledIn(Subjects.targetGraveyardCards(
                        null, GraveyardSearchScope.ALL_GRAVEYARDS, 3).fromASingleGraveyard()));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
