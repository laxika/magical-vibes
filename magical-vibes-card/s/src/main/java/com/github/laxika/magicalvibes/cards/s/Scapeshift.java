package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "MOR", collectorNumber = "136")
@CardRegistration(set = "M19", collectorNumber = "201")
@CardRegistration(set = "SPG", collectorNumber = "34")
public class Scapeshift extends Card {

    public Scapeshift() {
        addSpellEffect(SacrificePermanentsEffect.anyNumber(new PermanentIsLandPredicate(), Subjects.you()));
        addSpellEffect(new SearchLibraryEffect(new AmountThisWay(StepKind.SACRIFICE),
                new CardTypePredicate(CardType.LAND), LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
