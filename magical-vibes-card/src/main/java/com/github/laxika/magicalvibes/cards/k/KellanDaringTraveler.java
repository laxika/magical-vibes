package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.j.JourneyOn;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandOrMayGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "231")
@CardRegistration(set = "LCI", collectorNumber = "341")
public class KellanDaringTraveler extends Card {

    public KellanDaringTraveler() {
        setBackFaceCard(new JourneyOn());
        addCastingOption(new AdventureCast());
        addEffect(EffectSlot.ON_ATTACK, new RevealTopCardMatchingToHandOrMayGraveyardEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(3)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "JourneyOn";
    }
}
