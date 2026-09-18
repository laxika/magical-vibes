package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "113")
@CardRegistration(set = "ME2", collectorNumber = "130")
@CardRegistration(set = "A25", collectorNumber = "136")
@CardRegistration(set = "2XM", collectorNumber = "131")
@CardRegistration(set = "SLZ", collectorNumber = "61")
@CardRegistration(set = "SLZ", collectorNumber = "182")
@CardRegistration(set = "SLZ", collectorNumber = "303")
public class ImperialRecruiter extends Card {

    public ImperialRecruiter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPowerAtMostPredicate(2)))));
    }
}
