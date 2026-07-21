package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "134")
public class SidewinderNaga extends Card {

    public SidewinderNaga() {
        // As long as you control a Desert or there is a Desert card in your graveyard,
        // this creature gets +1/+0 and has trample.
        var desertCondition = new AnyOf(List.of(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
                new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.DESERT))
        ));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                desertCondition,
                new StaticBoostEffect(1, 0, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                desertCondition,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
