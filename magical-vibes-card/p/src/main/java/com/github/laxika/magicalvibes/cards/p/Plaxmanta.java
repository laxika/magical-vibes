package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "29")
@CardRegistration(set = "C15", collectorNumber = "100")
public class Plaxmanta extends Card {

    public Plaxmanta() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.enters(ObjectRelation.THIS), ConditionalEffect.unless(
                new NotCondition(new ColorSpentToCast(ManaColor.GREEN)), new SacrificeSelfEffect()));
    }
}
