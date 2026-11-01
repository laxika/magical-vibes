package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "SNC", collectorNumber = "121")
public class RiveteersRequisitioner extends Card {

    public RiveteersRequisitioner() {
        addCastingOption(AlternateHandCast.blitz("{2}{R}"));

        addStaticEffect(new ConditionalEffect(
                new CastForAlternateCost(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalEffect(
                new CastForAlternateCost(), new DrawCardEffect(1)));
    }
}
