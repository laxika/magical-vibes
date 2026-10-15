package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;

@CardRegistration(set = "LTC", collectorNumber = "496")
@CardRegistration(set = "LTC", collectorNumber = "540")
public class RohirrimChargers extends Card {

    public RohirrimChargers() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                "Exert Rohirrim Chargers as it attacks?"));

        addEffect(EffectSlot.ON_CONTROLLER_EXERTS,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT));
    }
}
