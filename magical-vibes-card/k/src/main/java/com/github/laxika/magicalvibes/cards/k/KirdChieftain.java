package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "151")
public class KirdChieftain extends Card {

    public KirdChieftain() {
        // This creature gets +1/+1 as long as you control a Forest.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));

        // {4}{G}: Target creature gets +2/+2 and gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{4}{G}",
                List.of(new BoostTargetCreatureEffect(2, 2), new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                "{4}{G}: Target creature gets +2/+2 and gains trample until end of turn.",
                TargetFilters.creature()));
    }
}
