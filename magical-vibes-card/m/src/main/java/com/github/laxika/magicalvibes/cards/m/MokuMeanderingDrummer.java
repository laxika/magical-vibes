package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "122")
public class MokuMeanderingDrummer extends Card {

    public MokuMeanderingDrummer() {
        // Whenever you cast a noncreature spell, you may pay {1}. If you do, Moku gets +2/+1
        // and creatures you control gain haste until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                UnlessPaysEffect.youMayPay("{1}", SequenceEffect.of(BoostEffect.self(2, 1), new GrantKeywordEffect(Keyword.HASTE, Subjects.creaturesYouControl())))
                        .withPrompt("Pay {1} to boost Moku and give your creatures haste?"));
    }
}
