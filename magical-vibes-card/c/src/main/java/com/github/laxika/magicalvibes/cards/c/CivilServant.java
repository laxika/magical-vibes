package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "176")
public class CivilServant extends Card {

    public CivilServant() {
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay(
                new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.CITIZEN), true),
                SequenceEffect.of(
                        BoostEffect.self(1, 0),
                        new GrantKeywordEffect(Keyword.LIFELINK, Subjects.source())))
                .withPrompt("Tap another untapped Citizen you control?"));
    }
}
