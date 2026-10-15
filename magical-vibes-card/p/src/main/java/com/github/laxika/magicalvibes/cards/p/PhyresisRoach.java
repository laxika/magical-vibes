package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "15")
public class PhyresisRoach extends Card {

    public PhyresisRoach() {
        CardSubtypePredicate insectCard = new CardSubtypePredicate(CardSubtype.INSECT);
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PerpetuallyGrantKeywordToMatchingCardsEffect(
                        insectCard,
                        new PermanentHasSubtypePredicate(CardSubtype.INSECT),
                        Set.of(Keyword.TOXIC),
                        null,
                        null,
                        insectCard,
                        insectCard));
    }
}
