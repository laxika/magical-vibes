package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "127")
public class CrimsonMuckwader extends Card {

    public CrimsonMuckwader() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)), new StaticBoostEffect(1, 1, GrantScope.SELF)));
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(new RegenerateEffect()), "{2}{B}: Regenerate Crimson Muckwader."));
    }
}
