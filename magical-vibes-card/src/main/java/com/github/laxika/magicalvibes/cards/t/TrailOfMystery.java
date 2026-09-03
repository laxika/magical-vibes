package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

@CardRegistration(set = "KTK", collectorNumber = "154")
public class TrailOfMystery extends Card {

    public TrailOfMystery() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsFaceDownPredicate(),
                        new MayEffect(
                                new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                                "Search your library for a basic land card?")));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 2, 2));
    }
}
