package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "134")
public class GrixisGrimblade extends Card {

    public GrixisGrimblade() {
        // As long as you control another multicolored permanent, Grixis Grimblade gets +1/+1 and has deathtouch.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsAnotherPermanent(new PermanentIsMulticoloredPredicate()), new StaticBoostEffect(1, 1, Set.of(Keyword.DEATHTOUCH), GrantScope.SELF)));
    }
}
