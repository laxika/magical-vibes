package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualBlitzEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "26")
public class RiveteersProvocateur extends Card {

    public RiveteersProvocateur() {
        addCastingOption(AlternateHandCast.blitz("{B}{R}"));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCardFromHandAndApplyPerpetualBlitzEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new CastForAlternateCost(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new CastForAlternateCost(), new SacrificeSelfAtEndStepEffect()));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalEffect(
                new CastForAlternateCost(), new DrawCardEffect(1)));
    }
}
