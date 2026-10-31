package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "210")
@CardRegistration(set = "FIN", collectorNumber = "406")
@CardRegistration(set = "FIN", collectorNumber = "551")
@CardRegistration(set = "FIN", collectorNumber = "568")
public class TravelingChocobo extends Card {

    public TravelingChocobo() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(new CardSubtypePredicate(CardSubtype.BIRD)));
        addStaticEffect(new ETBDoubleTriggerEffect(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardSubtypePredicate(CardSubtype.BIRD)))));
    }
}
