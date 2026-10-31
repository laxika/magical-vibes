package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "27")
@CardRegistration(set = "KHM", collectorNumber = "340")
public class SearchForGlory extends Card {

    public SearchForGlory() {
        addSpellEffect(new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardSupertypePredicate(CardSupertype.SNOW))),
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardSubtypePredicate(CardSubtype.SAGA)
        ))));
        addSpellEffect(new GainLifeEffect(new SnowManaSpentToCast()));
    }
}
