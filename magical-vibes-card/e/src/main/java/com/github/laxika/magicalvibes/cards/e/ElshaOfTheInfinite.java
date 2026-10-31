package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "2X2", collectorNumber = "208")
@CardRegistration(set = "C19", collectorNumber = "40")
public class ElshaOfTheInfinite extends Card {

    public ElshaOfTheInfinite() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(AllowCastFromTopOfLibraryEffect.withFlash(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
    }
}
