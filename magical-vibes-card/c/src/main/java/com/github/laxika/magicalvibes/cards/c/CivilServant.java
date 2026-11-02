package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "176")
public class CivilServant extends Card {

    public CivilServant() {
        addTrigger(Triggers.attacks(), new MayPayTapPermanentsEffect(
                new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.CITIZEN), true),
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "Tap another untapped Citizen you control?"));
    }
}
