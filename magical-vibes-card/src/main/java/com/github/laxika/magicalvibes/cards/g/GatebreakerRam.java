package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "126")
public class GatebreakerRam extends Card {

    public GatebreakerRam() {
        PermanentCount gatesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.GATE), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(gatesYouControl, gatesYouControl));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE), GrantScope.SELF)));
    }
}
