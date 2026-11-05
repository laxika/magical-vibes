package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "156")
public class RiveteersDecoy extends Card {

    public RiveteersDecoy() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());

        addCastingOption(AlternateHandCast.blitz("{3}{G}"));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new CastForAlternateCost()),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF),
                new SacrificeSelfAtEndStepEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new CastForAlternateCost()),
                new DrawCardEffect());
    }
}
