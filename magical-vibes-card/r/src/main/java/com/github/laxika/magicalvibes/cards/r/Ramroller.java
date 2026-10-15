package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ORI", collectorNumber = "237")
public class Ramroller extends Card {

    public Ramroller() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // This creature gets +2/+0 as long as you control another artifact.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsAnotherPermanent(new PermanentIsArtifactPredicate()), new StaticBoostEffect(2, 0, GrantScope.SELF)));
    }
}
