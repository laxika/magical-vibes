package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "162")
@CardRegistration(set = "DDS", collectorNumber = "21")
@CardRegistration(set = "C15", collectorNumber = "218")
public class FiremindsForesight extends Card {

    public FiremindsForesight() {
        addSpellEffect(SearchLibraryForOneCardOfEachSubtypeEffect.ofEach(
                instantWithManaValue(3), instantWithManaValue(2), instantWithManaValue(1)));
    }

    private static ChooseCardEffect.Each instantWithManaValue(int manaValue) {
        return new ChooseCardEffect.Each(new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.INSTANT),
                new CardMinManaValuePredicate(manaValue, false), new CardMaxManaValuePredicate(manaValue))),
                "instant card with mana value " + manaValue);
    }
}
