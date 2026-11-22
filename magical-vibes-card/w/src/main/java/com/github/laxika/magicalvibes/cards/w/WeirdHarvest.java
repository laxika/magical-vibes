package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "9ED", collectorNumber = "282")
@CardRegistration(set = "ONS", collectorNumber = "299")
public class WeirdHarvest extends Card {

    public WeirdHarvest() {
        addSpellEffect(EachPlayerSearchesLibraryEffect.maySearch(Subjects.eachPlayer(),
                new CardTypePredicate(CardType.CREATURE), new XValue(), LibrarySearchDestination.HAND));
    }
}
