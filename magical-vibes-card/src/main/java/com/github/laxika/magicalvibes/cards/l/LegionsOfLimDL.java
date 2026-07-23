package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "142")
public class LegionsOfLimDL extends Card {

    public LegionsOfLimDL() {
        // Snow swampwalk (This creature can't be blocked as long as defending player controls a snow Swamp.)
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW)
                ))
        ));
    }
}
